#!/usr/bin/perl -w

use strict;
use warnings;

use feature qw(say);

use Getopt::Long;
use Pod::Usage;

use DBI;
use Carp;
use IO::Handle;
use XML::Writer;

my $dbh;
my $data = '~/Downloads/CosmicCompleteExport_v63_300113.tsv';

my $help = 0;
my $verbose = 1;
my $db_host = "localhost";
my $db_name = "cosmic_data";
my $db_user = "root";
my $db_password;

GetOptions(
    'help|?' => \$help, 'verbose!' => \$verbose,
    'data-file=s' => \$data,
    'db-host=s' => \$db_host, 'db-name=s' => $db_name, 'db-user=s' => \$db_user, 'db-pass=s' => \$db_password
);
pod2usage(-exitval => 0, -verbose => 2) if ($help);

build();

sub execute_sql {
	my ($statement) = @_;
	say STDERR "$statement" if ($verbose);
	$dbh->do($statement) or die($dbh->errstr());
}

sub build {
	$dbh = DBI->connect("dbi:mysql:database=$db_name;host=$db_host",$db_user,$db_password);

    load_phase();
    patch_phase();
    duplicates_phase();
    frequencies_phase();
    output_phase();

    $dbh->disconnect();
}

sub load_phase {
    execute_sql(<<__ENDSQL__);
ALTER DATABASE
    DEFAULT CHARACTER SET 'latin1'
    DEFAULT COLLATE 'latin1_general_ci'
__ENDSQL__

    execute_sql(<<__ENDSQL__);
DROP TABLE IF EXISTS MUTATIONS
__ENDSQL__

    execute_sql(<<__ENDSQL__);
CREATE TABLE mutations (
  id INT PRIMARY KEY AUTO_INCREMENT,
  sample_name VARCHAR(40) NOT NULL,
  id_sample INT,
  id_tumour INT,
  primary_site VARCHAR(100) NOT NULL,
  site_subtype VARCHAR(250),
  primary_hist VARCHAR(150),
  hist_subtype VARCHAR(255),
  genome_wide_screen INT,
  gene_name VARCHAR(40),
  accession VARCHAR(30),
  hgnc CHAR(30),
  mutation_id INT,
  mutation_cds VARCHAR(180),
  mutation_aa VARCHAR(80),
  mutation_description VARCHAR(255),
  mutation_zygosity VARCHAR(30),
  ncbi36_position VARCHAR(30),
  ncbi36_strand CHAR(1),
  grch37_position VARCHAR(30),
  grch37_strand CHAR(1),
  somatic_status VARCHAR(50),
  pubmed CHAR(20),
  sample_source VARCHAR(20),
  tumour_origin VARCHAR(50),
  comments VARCHAR(1024),
  start INT,
  stop INT,
  chromosome CHAR(2),
  var_allele VARCHAR(64)
) ENGINE MyISAM;
__ENDSQL__

    execute_sql(<<__ENDSQL__);
LOAD DATA LOCAL INFILE '$data'
INTO TABLE mutations
IGNORE 1 LINES
(gene_name, accession, hgnc, sample_name, id_sample, id_tumour, primary_site, site_subtype, primary_hist, hist_subtype, 
 \@genome_wide_screen, 
 \@mutation_id, 
 mutation_cds, mutation_aa, 
 mutation_description, mutation_zygosity, 
 ncbi36_position, ncbi36_strand, grch37_position, grch37_strand, 
 somatic_status, pubmed, 
 sample_source, tumour_origin, comments)
SET mutation_id = IF(\@mutation_id='',0,\@mutation_id), 
    genome_wide_screen = IF(\@genome_wide_screen='',0,IF(\@genome_wide_screen='y',1,0))
__ENDSQL__

    execute_sql(<<__ENDSQL__);
ALTER TABLE mutations
ADD INDEX mutation_sample_name(sample_name(8)),
ADD INDEX mutation_id_sample(id_sample),
ADD INDEX mutation_id_idx(mutation_id),
ADD INDEX mutation_gene_mutation(gene_name(8), mutation_aa(8)),
ADD INDEX mutation_mutation(mutation_aa(8))
__ENDSQL__

    execute_sql(<<__ENDSQL__);
UPDATE mutations
SET chromosome = SUBSTRING_INDEX(grch37_position, ':', 1),
    start = CONVERT(SUBSTRING_INDEX(SUBSTRING_INDEX(grch37_position, ':', -1), '-', 1), SIGNED INTEGER),
    stop = CONVERT(SUBSTRING_INDEX(SUBSTRING_INDEX(grch37_position, ':', -1), '-', -1), SIGNED INTEGER),
    var_allele = 
      CASE
        WHEN mutation_cds REGEXP '^c\\\\.[0-9_]+ins[ACGT]+\$' THEN SUBSTRING_INDEX(mutation_cds, 'ins', -1)
        WHEN mutation_cds REGEXP '^c\\\\.[0-9_]+del.*\$' THEN ''
        WHEN mutation_cds REGEXP '^c\\\\.[0-9_]+[ACGT]+>[ACGT]+\$' THEN SUBSTRING_INDEX(mutation_cds, '>', -1)
        ELSE NULL
      END
WHERE grch37_position IS NOT NULL AND grch37_position != ''
__ENDSQL__
};

sub patch_phase {
    execute_sql(<<__ENDSQL__);
UPDATE mutations SET mutation_aa = "p.K120_F121insLYHKGLLK" WHERE mutation_aa = "pK120_F121insLYHKGLLK"
__ENDSQL__

    execute_sql(<<__ENDSQL__);
UPDATE mutations SET mutation_aa = "p.Q48_D54del" WHERE mutation_aa = "pQ48_D54del"
__ENDSQL__

    execute_sql(<<__ENDSQL__);
UPDATE mutations SET mutation_id = 0 WHERE mutation_aa = '' AND mutation_id != 0
__ENDSQL__

    execute_sql(<<__ENDSQL__);
UPDATE mutations SET primary_hist = hist_subtype WHERE primary_hist = "other"
__ENDSQL__

    execute_sql(<<__ENDSQL__);
UPDATE mutations SET mutation_aa = 'p.?' WHERE mutation_aa LIKE 'p.0%'
__ENDSQL__

    execute_sql(<<__ENDSQL__);
UPDATE mutations SET mutation_aa = 'p.?' WHERE mutation_aa = 'p.unknown'
__ENDSQL__

    execute_sql(<<__ENDSQL__);
DELETE FROM mutations WHERE mutation_cds LIKE 'c.?%' AND mutation_aa LIKE 'p.?%'
__ENDSQL__
}

sub duplicates_phase {
	
    execute_sql(<<__ENDSQL__);
ALTER TABLE mutations
ADD INDEX mutations_duplicate_idx(grch37_position, mutation_cds(30), mutation_id),
ADD INDEX mutation_genomic_idx(chromosome, start, stop, var_allele(2))
__ENDSQL__

    execute_sql(<<__ENDSQL__);
DROP TEMPORARY TABLE IF EXISTS genomic_duplicates
__ENDSQL__
    
    # Just to make things really fun, we can't even trust the CDS, because that might
    # different for the same actual mutation. This is because the CDS is typically 
    # different in a variant.
        
    execute_sql(<<__ENDSQL__);
CREATE TEMPORARY TABLE genomic_duplicates
SELECT chromosome, start, stop, var_allele, COUNT(DISTINCT mutation_cds) AS count
FROM mutations
GROUP BY chromosome, start, stop, var_allele
HAVING COUNT(DISTINCT mutation_cds) > 1
AND var_allele IS NOT NULL
__ENDSQL__
    
    execute_sql(<<__ENDSQL__);
ALTER TABLE genomic_duplicates
ADD COLUMN mutation_cds VARCHAR(180)
__ENDSQL__
    
    execute_sql(<<__ENDSQL__);
UPDATE genomic_duplicates gd
SET gd.mutation_cds = 
  (SELECT m.mutation_cds FROM mutations m
   WHERE m.chromosome = gd.chromosome AND m.start = gd.start AND m.stop = gd.stop AND m.var_allele = gd.var_allele
   ORDER BY LENGTH(m.gene_name) ASC
   LIMIT 1)
__ENDSQL__
    
    execute_sql(<<__ENDSQL__);
UPDATE mutations m
JOIN genomic_duplicates gd ON m.chromosome = gd.chromosome AND m.start = gd.start AND m.stop = gd.stop AND m.var_allele = gd.var_allele
SET m.mutation_cds = gd.mutation_cds
__ENDSQL__
    
    execute_sql(<<__ENDSQL__);
DROP TEMPORARY TABLE IF EXISTS duplicates
__ENDSQL__
	
    # Query set to identify mutations with the same genomic coordinates but different
    # mutation identifiers, and convert them to use the same mutation identifier. 

    execute_sql(<<__ENDSQL__);
CREATE TEMPORARY TABLE duplicates
SELECT DISTINCT m.grch37_position, m.mutation_cds, 
       (SELECT x.gene_name FROM mutations x 
        WHERE x.grch37_position = m.grch37_position AND x.mutation_cds = m.mutation_cds 
        ORDER BY LENGTH(x.gene_name) ASC LIMIT 1) AS gene_name,
       (SELECT y.mutation_id FROM mutations y 
        WHERE y.grch37_position = m.grch37_position AND y.mutation_cds = m.mutation_cds 
        ORDER BY COUNT(DISTINCT sample_name) DESC LIMIT 1) as mutation_id,
       (SELECT z.mutation_aa FROM mutations z 
        WHERE z.grch37_position = m.grch37_position AND z.mutation_cds = m.mutation_cds 
        ORDER BY COUNT(DISTINCT sample_name) DESC LIMIT 1) as mutation_aa
FROM (SELECT DISTINCT grch37_position, mutation_cds FROM mutations
      WHERE mutation_id != 0
      AND grch37_position is not null
      AND grch37_position != ''
      AND mutation_cds not like 'c.?%'
      GROUP BY grch37_position, mutation_cds
      HAVING COUNT(distinct mutation_id) > 1) AS m
__ENDSQL__

    execute_sql(<<__ENDSQL__);
UPDATE mutations m 
JOIN duplicates dups ON dups.grch37_position = m.grch37_position AND dups.mutation_cds = m.mutation_cds
SET m.mutation_id = dups.mutation_id, m.gene_name = dups.gene_name, m.mutation_aa = dups.mutation_aa
WHERE m.mutation_id != 0
AND m.grch37_position IS NOT NULL
AND m.grch37_position != ''
__ENDSQL__
}

sub frequencies_phase {
    execute_sql("DROP TABLE mutated_hist");
    execute_sql("DROP TABLE sample_hist");
    execute_sql("DROP TABLE mutation_freqs");
    execute_sql("DROP TABLE frequencies");

    execute_sql(<<__ENDSQL__);
CREATE TABLE mutated_hist (
  gene_name CHAR(30) ,
  mutation_aa CHAR(80),
  primary_site CHAR(150),
  primary_hist CHAR(150),
  mutated INT,
  KEY primary_site (primary_site(20)),
  KEY gene_name (gene_name(8)),
  KEY mutation_aa (mutation_aa(10)),
  KEY primary_hist (primary_hist(20))
) ENGINE MyISAM
__ENDSQL__

    execute_sql(<<__ENDSQL__);
CREATE TABLE sample_hist (
  gene_name CHAR(30) ,
  primary_site CHAR(150),
  primary_hist CHAR(150),
  samples INT,
  KEY primary_site(primary_site(20)),
  KEY gene_name(gene_name(8)),
  KEY primary_hist (primary_hist(20))
) ENGINE MyISAM
__ENDSQL__

    execute_sql(<<__ENDSQL__);
CREATE TABLE mutation_freqs (
  gene CHAR(30),
  mutation CHAR(80),
  tumor_tissue CHAR(150),
  histology_type CHAR(150),
  total_sample INT,
  sample_mutated INT,
  frequency FLOAT,
  INDEX (gene(8)),
  INDEX (mutation(10)),
  INDEX (tumor_tissue(20)),
  INDEX (histology_type(20))
) ENGINE MyISAM
__ENDSQL__

    execute_sql(<<__ENDSQL__);
CREATE TABLE frequencies (
  gene CHAR(30),
  mutation CHAR(80),
  tumor_type CHAR(150),
  total_sample INT,
  sample_mutated INT,
  frequency FLOAT,
  INDEX (mutation(10)),
  INDEX (tumor_type(20)),
  INDEX (total_sample),
  INDEX (frequency)
) ENGINE MyISAM
__ENDSQL__

    execute_sql(<<__ENDSQL__);
INSERT INTO mutated_hist 
SELECT gene_name, mutation_aa, primary_site, primary_hist, count(distinct(id_sample)) 
FROM mutations 
WHERE mutation_id != 0 
GROUP BY gene_name, mutation_aa, primary_site, primary_hist
__ENDSQL__

    execute_sql(<<__ENDSQL__);
INSERT INTO sample_hist 
SELECT gene_name, primary_site, primary_hist, count(distinct(id_sample)) 
FROM mutations 
GROUP BY gene_name, primary_site, primary_hist
__ENDSQL__

    execute_sql(<<__ENDSQL__);
INSERT INTO mutation_freqs 
SELECT s.gene_name, m.mutation_aa, s.primary_site, s.primary_hist, s.samples, m.mutated, m.mutated/s.samples
FROM mutated_hist AS m, sample_hist AS s 
WHERE s.gene_name = m.gene_name 
AND s.primary_site = m.primary_site 
AND s.primary_hist = m.primary_hist
__ENDSQL__

    execute_sql(<<__ENDSQL__);
INSERT INTO frequencies 
SELECT gene, SUBSTRING(mutation, 3) AS mutation, REPLACE(CONCAT(tumor_tissue, ' ', histology_type), "_", " ")  AS tumor_type, total_sample, sample_mutated, frequency 
FROM mutation_freqs
__ENDSQL__

    execute_sql(<<__ENDSQL__);
DELETE FROM frequencies 
WHERE mutation = '?'
__ENDSQL__

    execute_sql(<<__ENDSQL__);
CREATE INDEX frequencies_gene ON frequencies(gene(8))
__ENDSQL__

}

sub output_phase {
	my $output = IO::Handle->new_from_fd(\*STDOUT, "w");
	my $writer = XML::Writer->new(OUTPUT => $output, DATA_MODE => 1, DATA_INDENT => 2);

    execute_sql(<<__ENDSQL__);
CREATE TEMPORARY TABLE cosmic_gene (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name varchar(32) UNIQUE
) ENGINE MyISAM
__ENDSQL__

    execute_sql(<<__ENDSQL__);
CREATE TEMPORARY TABLE cosmic_tumour_type (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name varchar(150) UNIQUE
) ENGINE MyISAM
__ENDSQL__

    execute_sql(<<__ENDSQL__);
CREATE TEMPORARY TABLE cosmic_mutation (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name varchar(32),
  gene_id INT NOT NULL,
  UNIQUE KEY cosmic_mutation_mutation (gene_id, name)
) ENGINE MyISAM
__ENDSQL__

    execute_sql(<<__ENDSQL__);
INSERT INTO cosmic_gene (name)
SELECT DISTINCT UPPER(gene) AS name
FROM frequencies
__ENDSQL__

    execute_sql(<<__ENDSQL__);
INSERT INTO cosmic_tumour_type (name)
SELECT DISTINCT LOWER(tumor_type) AS name
FROM frequencies
__ENDSQL__
    
    execute_sql(<<__ENDSQL__);
INSERT INTO cosmic_mutation (name, gene_id)
SELECT DISTINCT mutation AS name, cosmic_gene.id as gene_id
FROM frequencies 
JOIN cosmic_gene ON cosmic_gene.name = UPPER(frequencies.gene)
__ENDSQL__

    my $mutations_statement = $dbh->prepare(<<__ENDSQL__) or die($dbh->errstr());
SELECT DISTINCT mutation_id, mutation_cds, grch37_position
FROM mutations
WHERE mutation_id != 0
AND gene_name = ? 
AND mutation_aa = ?
__ENDSQL__

    my $statement = $dbh->prepare(<<__ENDSQL__) or die($dbh->errstr());
SELECT DISTINCT cosmic_tumour_type.id,
                cosmic_tumour_type.name,
                cosmic_gene.id,
                cosmic_gene.name,
                cosmic_mutation.id, 
                cosmic_mutation.name, 
                frequencies.frequency, 
                frequencies.sample_mutated,
                frequencies.total_sample, 
                (((ROUND(frequencies.frequency * frequencies.total_sample)) * 
                  (ROUND(frequencies.frequency * frequencies.total_sample))) / 
                 frequencies.total_sample) * 2 AS relevance
FROM frequencies
JOIN cosmic_gene ON cosmic_gene.name = frequencies.gene
JOIN cosmic_mutation ON cosmic_mutation.name = frequencies.mutation AND cosmic_gene.id = cosmic_mutation.gene_id
JOIN cosmic_tumour_type ON cosmic_tumour_type.name = frequencies.tumor_type
WHERE frequencies.total_sample >= 20
ORDER BY cosmic_tumour_type.name, cosmic_gene.name, cosmic_mutation.name;
__ENDSQL__

    $writer->xmlDecl();
    $writer->startTag("frequencies");

    $statement->execute() or die($dbh->errstr());
    my $last_tumour_type_id = -1;
    my $last_gene_id = -1;
    while(my ($tumour_type_id, $tumour_type, $gene_id, $gene, $mutation_id, $mutation, $frequency, $mutated, $samples, $relevance) = $statement->fetchrow_array()) {
    	if ($tumour_type_id != $last_tumour_type_id) {
            $writer->endTag("gene") if ($writer->in_element('gene'));
    		$writer->endTag("tumour_type") if ($writer->in_element('tumour_type'));
    		$writer->startTag("tumour_type", name => $tumour_type);
    		$last_tumour_type_id = $tumour_type_id;
    		$last_gene_id = -1;
    	}
        if ($gene_id != $last_gene_id) {
            $writer->endTag("gene") if ($writer->in_element('gene'));
            $writer->startTag("gene", name => $gene);
            $last_gene_id = $gene_id;
        }
        
        $writer->startTag("mutation", name => $mutation);
        
        $mutations_statement->execute($gene, "p.$mutation") or die($dbh->errstr());
        while(my ($cosmic_id, $mutation_cds, $grch37_position) = $mutations_statement->fetchrow_array()) {
            if ($mutation_cds =~ m{^c\.(?<start>\d+)(?:_(?<stop>\d+))?(?:ins(?<insert>[ACGT]+)|del(?<delete>[ACGT]+)|(?<from>[ACGT]+)>(?<to>[ACGT]+))$}) {
                my $var_allele = $+{to} // $+{insert};
                my $ref_allele = $+{from} // $+{delete};
                $var_allele //= "" if (exists($+{delete}));
                $ref_allele //= "" if (exists($+{insert}));
                if (defined($var_allele) && defined($ref_allele) && $grch37_position =~ m{^(?<chromosome>\d+):(?<from>\d+)-(?<to>\d+)$}) {
                    my @attributes = (chromosome => $+{chromosome}, start => $+{from}, stop => $+{to});
                    push @attributes, ref_allele => $ref_allele;
                    push @attributes, var_allele => $var_allele;
                    push @attributes, cosmic_id => $cosmic_id if (defined($cosmic_id));
                    $writer->emptyTag('known', @attributes);
                }
            }
        }

        $writer->dataElement('frequency', $frequency);
    	$writer->dataElement('mutated', $mutated);
        $writer->dataElement('samples', $samples);
        $writer->dataElement('relevance', $relevance);
        $writer->endTag("mutation")
    }
    
    $writer->endTag("gene") if ($writer->in_element('gene'));
    $writer->endTag("tumour_type") if ($writer->in_element('tumour_type'));
    
    $writer->endTag("frequencies");
	$writer->end();
}

__END__
 
=head1 NAME
 
cosmic.pl - Generates the COSMIC frequency data XML file
 
=head1 SYNOPSIS
 
cosmic.pl [options] 
 
 Options:
   -help               brief help message
   -verbose            log as we go
 
=head1 OPTIONS
 
=over 8
 
=item B<-help>
 
Print a brief help message and exits.
 
=item B<-verbose>
 
Switches on more detailed progress logging
 
=back
 
=head1 DESCRIPTION
 
This program transforms the COSMIC data from a flat text file, the normal
form from the COSMIC download site at: ftp://ftp.sanger.ac.uk/pub/CGP/cosmic/data_export/,
and turns the CosmicCompleteExport_* file into an XML file of mutation frequencies
by gene, variant, and tumour type. This XML file can then be loaded into the
GPS application through its RESTful API. That way, the fairly complex
processing of frequencies doesn't have to happen within the application itself.

The script uses SQL extensively for this process, and requires access to a MySQL
server to run. That doesn't impose MySQL on the rest of the GPS application, and
if there is a way to use an embedded MySQL system, it will work just fine with
that. 

=head1 NOTES

=over 4

=item B<Loading the COSMIC data>

The database is loaded using LOAD DATA LOCAL INFILE, as documented by the
MySQL server, to bulk load the initial COSMIC data set. See the documentation
at: http://dev.mysql.com/doc/refman/5.1/en/load-data.html. Using LOCAL DATA
LOCAL INFILE supports bulk loading even when the server is running remotely.

=back

=head1 AUTHOR

Stuart Watt E<lt>stuart.watt@oicr.on.caE<gt>

=head1 COPYRIGHT

This software is copyright (c) 2013 by the Ontario Institute for Cancer Research.

This library is free software and may be distributed under the same terms as Perl itself.

=cut
