SRC = report
TARGET = $(SRC).pdf

all: 	report

report: refs
	pdflatex $(SRC).tex

refs:	report-no-refs
	bibtex report

report-no-refs:
	pdflatex $(SRC).tex

view: 	report
	nohup okular $(TARGET) &>/dev/null &
	rm -f nohup.out

edit:
	gvim $(SRC).tex

.PHONY: clean
clean:
	rm -rf *.log *.aux *.toc 'Makefile~' 'nohup.out' *.bbl *.blg
