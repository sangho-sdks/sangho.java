# Détection automatique Windows vs Unix
ifeq ($(OS),Windows_NT)
    MVN := mvn.cmd
    RM  := rmdir /s /q
else
    MVN := mvn
    RM  := rm -rf
endif

compile:
	$(MVN) compile

test:
	$(MVN) test

test-class:
	$(MVN) test -Dtest=$(CLASS)

package:
	$(MVN) package -DskipTests

package-sources:
	$(MVN) package source:jar -DskipTests

publish-local:
	$(MVN) install -DskipTests

publish:
	$(MVN) deploy

clean:
	$(MVN) clean

.PHONY: compile test test-class package package-sources publish-local publish clean
