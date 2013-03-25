#!/usr/bin/perl -w

use strict;
use warnings;

use feature qw(say);

use File::Find;
use File::Spec;
use XML::Parser;
use Getopt::Long;
use Pod::Usage;

my $base = glob(qw(~/pubmed));
my $bases = qr/[ACGT]/;
my $acids = qr/[ACDEGFHIKLMNPQRSTWVY*]/;
my $longAcids = qr/(?:Ala|Arg|Asn|Asp|Cys|Glu|Gln|Gly|His|Ile|Lys|Met|Phe|Pro|Leu|Ser|Thr|Tyr|Trp|Val|X)/;

my $help = 0;
my $verbose = 0;

GetOptions('help|?' => \$help, 'verbose!' => \$verbose, 'pubmed-base' => \$base);
pod2usage(-exitval => 0, -verbose => 2) if ($help);

sub process {
	my $file = $File::Find::name;
	my $dir = $File::Find::dir;
	return unless ($file =~ m{\.nxml$});
	
	my $rel = File::Spec->abs2rel($file, $dir);
	$rel =~ s{\.nxml$}{};
	$rel =~ s{_}{ }g;
	
    my @segments = ();

	my $parser = XML::Parser->new(
	   NoLWP => 1, 
	   NoExpand => 1,
	   Handlers => {
	       Char => sub { 
	       	   my ($p, $el) = @_; 
	       	   push @segments, $el; }
	   });
    open(my $fh, '<', $file) or die "Couldn't open $file: $!";
    $parser->parse($fh);
    close($fh);
	
	my $text = join(" ", @segments);
	$text =~ s{ ([><&'"]) }{$1}g;    # Hack - using segments always delimits character entities, so remove that
	
    if ($text !~ m{\bgene\b}i || $text !~ m{\b(?:mutation|mutated|variant)\b}i) {
        say STDERR "Skipping non-genomic file: $rel" if ($verbose);
        return;
    }
    
	my $words =()= $text =~ m{[A-Za-z]{3,}}g;
    my $numbers =()= $text =~ m{[0-9.]{3,}}g;
    
    if ($words < $numbers * 4) {
    	say STDERR "Skipping data-rich file: $rel" if ($verbose);
    	return;
    }
    
    say STDERR $rel if ($verbose);
    
    my ($year) = ($rel =~ m{\b(\d{4,4})\b});
	
    while ($text =~ m{\b(${bases}{4,})}gs) {
        say STDOUT "$year,$rel,1,$1";
    }

	while ($text =~ m{\bc\.([1-9]\d{1,3}(?:[_-][1-9]\d{1,3})?(?:dup${bases}+|ins${bases}+|del${bases}+|${bases}+>${bases}+))}gs) {
		say STDOUT "$year,$rel,2,c.$1";
	}

    while ($text =~ m{\b(${acids}[1-9]\d{1,3}${acids}|${acids}[1-9]\d{1,3}(?:_${acids}[1-9]\d{1,3})?(?:fs|ins${acids}+|del${acids}+|${acids}+>${acids}+))}gs) {
        say STDOUT "$year,$rel,3,p.$1";
    }
    
    while ($text =~ m{\b(${longAcids}[1-9]\d{1,3}${longAcids}|${longAcids}[1-9]\d{1,3}(?:_${longAcids}[1-9]\d{1,3})?(?:fs|ins${longAcids}+|del\d{1,4}|${longAcids}+>${longAcids}+))}gs) {
        say STDOUT "$year,$rel,3,p.$1";
    }
};

find { wanted => \&process, no_chdir => 1 }, $base;

__END__
 
=head1 NAME
 
pubmed.pl - Identifies references to HGVS and similar variants in PubMed
 
=head1 SYNOPSIS
 
pubmed.pl [options] 
 
 Options:
   -help               brief help message
   -verbose            log as we go
   -pubmed-base="..."  base directory for a PubMed archive
 
=head1 OPTIONS
 
=over 8
 
=item B<-help>
 
Print a brief help message and exits.
 
=item B<-verbose>
 
Switches on more detailed progress logging
 
=item B<-pubmed-base>="..."
 
Specifies the base directory for PubMed. By default, this is a
directory "pubmed" in the current user's home directory.
 
=back
 
=head1 DESCRIPTION
 
This program will read through PubMed identifying references 
to HGVS and similar variants in PubMed.

The output (on standard output) is comma-separated values data
with four columns: 

=over 4

=item B<year>

A four digit year string.

=item B<source>

A full journal reference, usually comprising name, year, volume, 
issue, and pages, in free text.

=item B<variant type>

An integer type, 1 for a string of bases, 2 for an HGVS DNA-level mutation
string, 3 for a protein-level mutation string using short form base letters, 
and 3 for a protein-level mutation string using long form base abbreviations.

=item B<variant string>

The variant itself, as a string.

=back

You will probably want to use UNIX tools like C<sort>, C<uniq>, C<join> to
analyse these files. Alternatively, read them into an SQL database for deeper
analysis. 

=head1 AUTHOR

Stuart Watt E<lt>stuart.watt@oicr.on.caE<gt>

=head1 COPYRIGHT

This software is copyright (c) 2013 by the Ontario Institute for Cancer Research.

This library is free software and may be distributed under the same terms as Perl itself.

=cut