# Changelog

All notable changes to this project from 2026-05-19 onward will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased

## [4.0.6] - 2026-06-09

* bump dependencies for transitive CVEs, appbase -> 4.0.6, tomcat -> 11.0.24

## [4.0.5] - 2026-05-27

* bump appbase version in order to pass incoming x-request-id header to remote sparql source

## [4.0.4] - 2026-05-19

### Security

* Update appbase (4.0.4), appbase-security (5.0.2) and armlib (1.0.4) stack to fix transitive vulns
* Update tomcat to 11.0.22 for vulns
