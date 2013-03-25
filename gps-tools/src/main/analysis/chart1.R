library(ggplot2)
library(plyr)

cosmic <- read.table("/Users/swatt/Dropbox/PublicationDataAnalysis/cosmic.csv", header = T,  sep = ",")
cosmic <- mutate(cosmic, date = as.Date(cosmic$date, "%d-%b-%y"))
cosmic <- mutate(cosmic, days = as.numeric(cosmic$date - as.Date("01/01/06", "%m/%d/%y"), units="days"))
cosmic <- mutate(cosmic, log_count = log(cosmic$count))

frequencies <- read.table("/Users/swatt/Dropbox/PublicationDataAnalysis/mutation_frequencies.csv", header = T, sep = ",")
frequencies <- mutate(frequencies, days = 365 * (year - 1996))
frequencies <- mutate(frequencies, log_all = log(all + 1))
frequencies <- mutate(frequencies, date = as.Date(sprintf("01/01/%d", frequencies$year), format = "%d/%m/%Y"))

genbank <- read.table("/Users/swatt/Dropbox/PublicationDataAnalysis/genbank.csv", header = T,  sep = ",")
genbank <- mutate(genbank, date = as.Date(sprintf("01-%s", genbank$date), "%d-%b-%Y"))
genbank <- mutate(genbank, days = as.numeric(genbank$date - as.Date("01/01/06", "%m/%d/%y"), units="days"))
genbank <- mutate(genbank, log_count = log(genbank$entries))

sra <- read.table("/Users/swatt/Dropbox/PublicationDataAnalysis/sra_stat_unix.csv", header = T,  sep = ",")
sra <- mutate(sra, date = as.Date(sra$date, "%m/%d/%Y"))
yscale = c(1000, 1000000000)
myscale = yscale * 1000000
dates = c(as.Date("01/01/2000", "%d/%m/%Y"), as.Date("01/01/2013", "%d/%m/%Y"))
altpoints = c(1000, 1000000, 1000000000)

par(mar=c(5,4,2,4)) 
plot(cosmic$date, cosmic$count, pch=1, xlim = dates, ylim=yscale, col = "red", cex = 0.6, xlab="", ylab="", log="y")
title(main="The Curation Crunch", col.main="darkgreen", xlab="Date", ylab="Count", col.lab="black", cex.lab=1)
mtext("SRA Base Count", at=c(1000000), line = 2.5, side = 4)
par(new=T)
plot(genbank$date, genbank$entries, pch=2, xlim = dates, ylim=yscale, col= "darkgreen", cex = 0.6, xaxt="n", yaxt="n", xlab="", ylab="",log="y")
par(new=T)
plot(frequencies$date, frequencies$protein, pch=4, xlim = dates, ylim=yscale, col= "darkblue", cex = 0.6, xaxt="n", yaxt="n", xlab="", ylab="",log="y")
par(new=T)
lines(sra$date, sra$bases / 1000000, lwd = 2, xlim = dates, ylim=myscale, col = "black", cex = 0.6, log="y") 
axis(4, at = altpoints, labels = altpoints * 1000000, col.axis="black")
legend(as.Date("01/01/2000", "%d/%m/%Y"),100000,legend = c("COSMIC", "GenBank" ,"PubMed", "SRA"), bg = "white", pch=c(1, 2, 4, 5), lwd=c(0, 0, 0, 2), pt.lwd=c(1, 1, 1, 0), ncol=1, text.col=c("red","darkgreen","darkblue", "black"), col=c("red","darkgreen","darkblue", "black"), pt.cex=c(1, 1, 1, 0))

