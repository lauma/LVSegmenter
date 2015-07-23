#!/bin/bash

/Library/Java/JavaVirtualMachines/jdk1.8.0_25.jdk/Contents/Home/bin/java -Xmx4g -Dfile.encoding=UTF-8 -classpath bin:dist/*:lib/*:lib/commons-collections4-4.0/commons-collections4-4.0.jar:lib/commons-lang3-3.4/commons-lang3-3.4.jar lv.ailab.domainnames.AlternativeBuilder names-cleaned.txt names-alternatives.txt lv_lemmas_70p.out polyglot_en.out sinonimi.txt lv=wordlist-filtered-lv.txt en=wordsEn-sil-filtered.txt




# bin:lib/commons-collections4-4.0/commons-collections4-4.0-javadoc.jar:lib/commons-collections4-4.0/commons-collections4-4.0-sources.jar:lib/commons-collections4-4.0/commons-collections4-4.0-test-sources.jar:lib/commons-collections4-4.0/commons-collections4-4.0-tests.jar:lib/commons-collections4-4.0/commons-collections4-4.0.jar:lib/commons-lang3-3.4/commons-lang3-3.4-javadoc.jar:lib/commons-lang3-3.4/commons-lang3-3.4.jar:lib/org.restlet.ext.json.jar:lib/org.restlet.jar:lib/org.json.jar:/Applications/eclipse/plugins/org.junit_4.11.0.v201303080030/junit.jar:/Applications/eclipse/plugins/org.hamcrest.core_1.3.0.v201303031735.jar DomainServer