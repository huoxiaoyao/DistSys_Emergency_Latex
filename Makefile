SRC = report.tex
TARGET = $(SRC:.tex=.pdf)

all: 	subj

subj:
	pdflatex $(SRC)

view: 	subj
	nohup okular $(TARGET) &>/dev/null &
	rm -f nohup.out

edit:
	gvim $(SRC)

.PHONY: clean
clean:
	rm -rf *.log *.aux *.toc *.pdf
