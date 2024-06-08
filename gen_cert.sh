#!/bin/sh
KEYSTORE=src/main/resources/ssl/keystore
PASSWORD=12345678
SUBJECT="/C=US/ST=Texas/O=FLL Scorer/OU=FLL Scorer/CN=fllscorer.local"
rm -f src/main/resources/ssl/keystore
openssl genrsa -des3 -passout pass:${PASSWORD} -out jetty.key
openssl req -new -x509 -days 3650 -subj "${SUBJECT}" -passin pass:${PASSWORD} \
        -key jetty.key -passout pass:${PASSWORD} -out jetty.crt
keytool -import -noprompt -keystore ${KEYSTORE} -storepass ${PASSWORD}        \
        -alias jetty -file jetty.crt -trustcacerts
openssl req -new -subj "${SUBJECT}" -passin pass:${PASSWORD} -key jetty.key   \
        -passout pass:${PASSWORD} -out jetty.csr
openssl pkcs12 -export -passin pass:${PASSWORD} -inkey jetty.key              \
        -in jetty.crt -passout pass:${PASSWORD} -out jetty.pkcs12
keytool -importkeystore -srckeystore jetty.pkcs12 --srcstorepass ${PASSWORD}  \
        -srcstoretype PKCS12 -destkeystore ${KEYSTORE}                        \
        -deststorepass ${PASSWORD}
rm -f jetty.crt jetty.csr jetty.key jetty.pkcs12