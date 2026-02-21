# KMRTD

**Kotlin Machine Readable Travel Documents** — A modern Kotlin port of [JMRTD](https://jmrtd.org), the open-source Java library for accessing Machine Readable Travel Documents (MRTD) as specified by [ICAO Doc 9303](https://www.icao.int/publications/pages/publication.aspx?docnum=9303).

---

## What is KMRTD?

KMRTD brings the power of JMRTD into the Kotlin ecosystem with idiomatic APIs, sealed error handling, and coroutine support. If you've ever integrated JMRTD into an Android app and struggled with blocking calls, nullable-everything APIs, and Java-era exception handling — this library is for you.

KMRTD enables reading and verifying electronic identity documents (ePassports, national ID cards) via NFC, supporting the cryptographic protocols required for secure chip communication.

## Features

- 🔐 **PACE & BAC authentication** — Full support for both Password Authenticated Connection Establishment and Basic Access Control protocols
- 📖 **Data Group reading** — Read biometric data, MRZ info, facial images, and more from ICAO-compliant chips
- 🛡️ **Passive & Active Authentication** — Verify document authenticity and detect clones
- 🇮🇹 **CIE & ePassport tested** — Battle-tested with Italian Electronic Identity Cards (CIE 3.0) and ePassports
- 🧩 **Kotlin-first API** — Sealed classes, data classes, extension functions, null safety by design
- ⚡ **Coroutine-ready** — Suspend functions for NFC operations, Flow for reading state
- 📦 **Drop-in replacement** — Same underlying ICAO compliance, modernized API surface

## Why not just use JMRTD?

JMRTD is an excellent library, but it was designed in a Java SE world. Integrating it into modern Android development means:

- Wrapping every call in coroutines manually
- Dealing with pervasive nullability without any compiler guidance
- Catching generic exceptions instead of handling typed errors
- Writing boilerplate adapters for Compose/Flow-based UIs

KMRTD solves all of this at the library level, so you don't have to, also, Kmrtd offer helper and utility methods for simplify develop (also allow to jsonify all!)

## Quick Start

```kotlin
// Coming soon — API under active development
```

## Project Status

🚧 **Work in progress** — Phase 1 (mechanical Kotlin conversion) is complete. Phase 2 (idiomatic Kotlin refactoring) is underway.

| Phase | Status | Description |
|-------|--------|-------------|
| 1. Kotlin Conversion | 🔄 In Progress | 1:1 port of all JMRTD Java sources to Kotlin |
| 2. Idiomatic Refactoring | 🔄 In Progress | Null safety, sealed classes, data classes, enums |
| 3. Coroutine Support | ⏳ Planned | Suspend functions, Flow-based APIs |
| 4. Android Module | ⏳ Planned | Lifecycle-aware NFC sessions, Compose state |
| 5. Documentation & Publishing | ⏳ Planned | KDoc, Maven Central, sample app |

## Based On

This project is a Kotlin port of [JMRTD 0.8.4](https://jmrtd.org) (released February 2026), originally developed by Martijn Oostdijk and the JMRTD team. All credit for the underlying ICAO Doc 9303 implementation goes to the original authors.

## Dependencies

- [SCUBA](https://sourceforge.net/projects/scuba/) — Smart Card Utils for Better Access
- [Bouncy Castle](https://www.bouncycastle.org/) — Cryptographic provider
- [cert-cvc](https://github.com/Keyfactor/ejbca-cert-cvc) — Card Verifiable Certificates

## License

LGPL 3.0 — Same as the original JMRTD library.

See [LICENSE](LICENSE) for details.

## Author

**Alessandro Giaquinto** — Android Engineer specializing in NFC/RFID document verification.

- [LinkedIn](https://www.linkedin.com/in/giaquale/)
- [GitHub](https://github.com/giaquale)