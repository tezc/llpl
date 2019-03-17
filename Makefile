# 
# Copyright (C) 2018 Intel Corporation
#
# SPDX-License-Identifier: BSD-3-Clause
# 
#

CC = g++
ROOT_DIR:=$(shell dirname $(realpath $(lastword $(MAKEFILE_LIST))))

JNI_INCLUDES = $(JAVA_HOME)/include $(JAVA_HOME)/include/linux

CFLAGS = -O3 -DNDEBUG -fPIC
LINK_FLAGS = -fPIC -O3 -DNDEBUG -shared -lpmem -lpmemobj -Wl,-rpath,/usr/local/lib:/usr/local/lib64

CPP_SOURCE_DIR = src/main/cpp
TARGET_DIR = bin
CPP_BUILD_DIR = $(TARGET_DIR)/cppbuild
BASE_CLASSPATH = $(CLASSES_DIR):lib
ARCH = $(shell $(CC) -dumpmachine | awk -F'[/-]' '{print $$1}')
ALL_CPP_SOURCES = $(wildcard $(CPP_SOURCE_DIR)/*.cpp)
ALL_OBJ = $(addprefix $(CPP_BUILD_DIR)/, $(notdir $(ALL_CPP_SOURCES:.cpp=.o)))
SO_FILE_NAME = libllpl-$(ARCH).so
LIBRARIES = $(addprefix $(CPP_BUILD_DIR)/, $(SO_FILE_NAME))


all: sources
sources: cpp
cpp: $(LIBRARIES)

clean:
	rm -rf $(TARGET_DIR)

$(LIBRARIES): | $(CPP_BUILD_DIR)
$(ALL_OBJ): | $(CPP_BUILD_DIR)

$(CPP_BUILD_DIR)/%.so: $(ALL_OBJ)
	$(CC) -Wl,-soname,$@ -o $@ $(ALL_OBJ) $(LINK_FLAGS)
	cp $(CPP_BUILD_DIR)/$(SO_FILE_NAME) $(ROOT_DIR)/src/main/resources/lib/$(SO_FILE_NAME)

$(CPP_BUILD_DIR)/%.o: $(CPP_SOURCE_DIR)/%.cpp
ifndef JAVA_HOME
	$(error JAVA_HOME not set)
endif
	$(CC) $(CFLAGS) $(addprefix -I, $(JNI_INCLUDES)) -o $@ -c $<

$(CPP_BUILD_DIR):
	mkdir -p $(CPP_BUILD_DIR)
