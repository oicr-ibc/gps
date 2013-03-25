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

par(mar=c(8,4,4,4)) 
plot(cosmic$date, cosmic$count, pch=1, xlim = dates, ylim=yscale, col = "red", cex = 0.6, xlab="Date", ylab="Count", log="y")
title(main="My Title", col.main="red", sub="My Sub-title", col.sub="blue", xlab="My X label", ylab="My Y label",col.lab="green", cex.lab=0.75)
mtext("Hello", at=c(2000, 2000), side = 4)
par(new=T)
plot(genbank$date, genbank$entries, pch=2, xlim = dates, ylim=yscale, col= "darkgreen", cex = 0.6, xaxt="n", yaxt="n", xlab="", ylab="",log="y")
par(new=T)
plot(frequencies$date, frequencies$protein, pch=4, xlim = dates, ylim=yscale, col= "blue", cex = 0.6, xaxt="n", yaxt="n", xlab="", ylab="",log="y")
par(new=T)
lines(sra$date, sra$bases / 1000000, lwd = 2, xlim = dates, ylim=myscale, col = "black", cex = 0.6, log="y") 
axis(4, at = altpoints, labels = altpoints * 1000000, col.axis="black")
legend(as.Date("01/01/2000", "%d/%m/%Y"),100000,legend = c("COSMIC", "Genbank" ,"Pubmed", "SRA"), bg = "white", pch=c(1, 2, 4, 5), lwd=c(0, 0, 0, 2), pt.lwd=c(1, 1, 1, 0), ncol=1, text.col=c("red","darkgreen","blue", "black"), col=c("red","darkgreen","blue", "black"), pt.cex=c(1, 1, 1, 0))


lines(x, lwd = 2 , col = "red") # , col = "red")
plot(y, pch=2, ylim=c(0.00001,2.3025851), col= "green",xaxt="n", yaxt="n", xlab="", ylab="",log="y")
lines(y, lwd = 2 , col = "green") # , col = "red")
par(new=T)
plot(z, pch=3, ylim=c(0.00001,2.3025851), col = "blue",xaxt="n", xlab="", yaxt="n", ylab="",log="y")
lines(z, lwd = 2, col = "blue") # , col = "red")
#axis(1, at=c(1,2,3,4,5,6,7),labels=cig_labels)
legend(2,100,legend = c("BFAST", "BLAT" ,"bowtie2"), bg = "white",pch=c(1:7),ncol=1,pt.cex=1, text.col=c("red","green","blue"))



ggplot(data = cosmic, aes(date, count)) + 
  geom_point() + 
  scale_x_date() + 
  scale_y_log10(limits = c(1e4,1e6)) + 
  stat_smooth(method="lm")
  
ggplot(data = cosmic, aes(days, log_count)) + 
  geom_point() + 
  stat_smooth(method="lm")

fit <- lm(log_count ~ days, data = cosmic, start = list(a = 0, b = 0), trace = TRUE) 

summary(fit)



ggplot(data = genbank, aes(date, entries)) + 
  geom_point() + 
  scale_x_date() + 
  scale_y_log10(limits = c(1e5,1e9)) + 
  stat_smooth(method="lm") + opts(title = expression("Genbank growth over time"))
  
ggplot() +
  geom_point(data = cosmic, aes(date, count), colour = "darkgreen", shape = 1) +
  geom_point(data = genbank, aes(date, entries), colour = "red", shape = 2) +
  geom_point(data = frequencies, aes(date, protein), shape = 5) +
  geom_line(data = sra, aes(date, bases / 1000000), size = 1) +
  scale_x_date() + 
  scale_y_log10(limits = c(1e2,1e9)) + 
  stat_smooth(method="lm") +
  opts(legend.justification = c(0,1), legend.position = c(0,1), title = "X")
  
legend(1998, 1000, legend = c("non-junctions","junctions", "X", "Y"),fill = c("blue","violet", "red", "green"),bg = "white",ncol=1,cex=0.8,)
