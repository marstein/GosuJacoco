#!/usr/bin/python
# Read suites.xml files, extract suite names

import urllib
from xml.etree.cElementTree import parse

import sys


def print_suite_names(suite_file):
    #print "Parsing file %s" % suite_file
    suitefile = parse(suite_file).getroot()
    for suite in suitefile.findall('suite'):
        print suite.attrib["id"]


for i in range(1, len(sys.argv)):
    print_suite_names(sys.argv[i])
