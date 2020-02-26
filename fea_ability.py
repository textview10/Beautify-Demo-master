#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys

def rewriteBeautifyHeader(inFileName,outFileName,avaliable_fea):
	#读取现有的头文件
	fp = open(inFileName,"r")
	inFile = []
	beautify_type_start = False
	lines = fp.readlines()
	#写入outFileName
	with open(outFileName,"w") as file_object:
		for line in lines:
			if 'def ABILITY_STRING' in line:
				fea_ability = "    def ABILITY_STRING = \"\\\""
				fea_ability += avaliable_fea
				fea_ability += "\\\"\"\n"
				file_object.write(fea_ability)
			else:
				file_object.write(line)


if __name__ == '__main__':
	rewriteBeautifyHeader('./app/build.gradle','./app/build_new.gradle',sys.argv[1])