#!/bin/sh

rm -f pubmed.csv pubmed.log
perl pubmed.pl --verbose > pubmed.csv 2> pubmed.log
rm -f pubmed_uniq.csv pubmed.db mutation_frequencies.csv

# No need to sort, as the script generating the data works depth first and 
# sequentially

uniq pubmed.csv pubmed_uniq.csv


sqlite3 pubmed.db <<EOF >mutation_frequencies.csv
.mode csv 
.echo off 
create table pubmed (year int, article varchar(255), code int, variant varchar(255));
.import pubmed_uniq.csv pubmed
.headers on
select y1.year, y1.genomic + y2.protein as "all", y1.genomic, y2.protein, 100.0 * y1.genomic / (y1.genomic + y2.protein) as "percent_genomic"
from (select year, count(*) as genomic
      from pubmed
      where code = 2
      group by year) as y1
join (select year, count(*) as protein
      from pubmed
      where code = 3
      group by year) as y2 on y1.year = y2.year;
EOF
