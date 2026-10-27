# GlobalSign Root R46

`globalsign-root-r46.crt` is the self-signed GlobalSign Root R46 CA certificate,
stored in PEM format. It was downloaded from
[GlobalSign](https://secure.globalsign.com/cacert/rootr46.crt), listed in the
[official root certificate directory](https://support.globalsign.com/ca-certificates/globalsign-root-certificates).

- Subject and issuer: `CN=GlobalSign Root R46, O=GlobalSign nv-sa, C=BE`
- Serial number: `11D2BBB9D723189E405F0A9D2DD0DF2567D1`
- Valid until: 20 March 2046
- SHA-256: `4FA3126D8D3A11D1C4855A4F807CBAD6CF919D3A5A88B03BEA2C6372D93C40C9`

The SHA-256 fingerprint was checked against the
[Mozilla CA inclusion record](https://ccadb.my.salesforce-sites.com/mozilla/PrintViewForCase?CaseNumber=00000469).
Inspect the bundled certificate with:

```bash
keytool -printcert -file certs/globalsign-root-r46.crt
```

The Docker build imports this certificate into a copy of the runtime image's Java
truststore, preserving its existing trusted certificates. The resulting truststore
replaces `/etc/ssl/certs/java/cacerts`, the runtime's default Java truststore.
This also works when Kubernetes overrides `JAVA_TOOL_OPTIONS`. An explicit
`-Djavax.net.ssl.trustStore` setting takes precedence over this default.

Rebuild and redeploy the image after replacing the certificate. No certificate is
downloaded during the image build.
