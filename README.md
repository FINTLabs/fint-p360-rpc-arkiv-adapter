# FINT P360 adapter
This adapter connects TietoEvry Public 360 to FINT.

It uses the SIF RPC service.

The adapter uses the following services:
- CaseService
- DocumentService
- ContactService
- FileService
- SupportService

# Properties

| Properties                                            | Default                                  | Description   |
|:------------------------------------------------------|:-----------------------------------------| :------------ |
| fint.p360.clientid                                    |                                          |               |
| fint.p360.authkey                                     |                                          |               |
| fint.p360.endpoint-base-url                           |                                          |               |
| fint.p360.file.version-format.ignore                  | `true`                                   |               |
| fint.file-repository.cache-directory                  | file-cache                               |               |
| fint.file-repository.cache-spec                       | expireAfterAccess=5m,expireAfterWrite=7m |               |
| fint.kulturminne.tilskudd-fartoy.arkivdel             |                                          |               |
| fint.kulturminne.tilskudd-fartoy.sub-archive          |                                          |               |
| fint.kulturminne.tilskudd-fartoy.keywords             |                                          |               |
| fint.kulturminne.tilskudd-fartoy.achive-code-type     |                                          |               |
| fint.kulturminne.tilskudd-fartoy.intitial-case-status | `B`                                      |               |

# Code Tables
| Code Table                                            | Default P360 Table                            | NOARK code                |
| :---------------------------------------------------- | :-------------------------------------------- | :------------------------ |
| fint.p360.tables.access-code                          | code table: Access code                       | Tilgangsrestriksjon       |
| fint.p360.tables.case-contact-role                    | code table: Contact - Case role               | PartRolle                 |
| fint.p360.tables.case-status                          | code table: Case status                       | SaksStatus                |
| fint.p360.tables.contact-role                         | code table: Activity - Contact role           | KorrespondansepartType    |
| fint.p360.tables.document-category                    | code table: Document category                 | JournalpostType           |
| fint.p360.tables.document-relation                    | Attribute value: VersionFile - ToRelationType | TilknyttetRegistreringSom |
| fint.p360.tables.document-status                      | code table: FileStatus                        | Dokumentstatus            |
| fint.p360.tables.document-type                        | code table: File category                     | Dokumenttype              |
| fint.p360.tables.journal-status                       | code table: Journal status                    | JournalStatus             |
| fint.p360.tables.law                                  | code table: Law                               | Skjermingshjemmel         |
| fint.p360.tables.note-type                            | code table: Note type                         | Merknadstype              |
| fint.p360.tables.version-format                       | Attribute value: File - ToVersionFormat       | Variantformat             |

# OData filter support
This adapter have support for OData filtering of cases. That means it's now possible to
get cases based on a OData filter, not only `mappeid`, `systemid` and `soknadsnummer`.
The old filter (query param `title`) is now deprecated and will be removed, use `$filter=tittel eq 'Tittel'` instead!

We currently support `mappeid`, `tittel`, `systemid`, `arkivdel`, `klassifikasjon/primar/verdi` and `kontaktid`.

### Examples
- `$filter=arkivdel eq '1337'`
- `$filter=tittel eq 'Charlie Foxtrot - S/S Den Sorte Dame'`
- `$filter=mappeid eq '2024/123'`
- `$filter=systemid eq '123456'`
- `$filter=klassifikasjon/primar/verdi eq 'C52'`
- `$filter=kontaktid eq '08089312345'`
