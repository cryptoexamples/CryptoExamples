---
title: Introduction
permalink: index.html
---

# Introduction

Many applications need cryptography. There are many examples in the web, that are either insecure or do not work right away.
As programmers are usually not cryptography or security experts, they should be able to take the path of least resistance and not have to bother with all the decisions needed to make cryptography really secure.
The **crypto examples provided on this site meet current security and cryptography requirements**.
They demonstrate how cryptography can be used in many programming languages for common use cases like encrypting a String or a file using symmetric or asymmetric encryption.

## Available programming languages and use cases

{% for page in site.pages %}
{% if page.sort_weight == 1 %}
- [{{ page.title }}]({{ page.url }})
{% endif %}
{% endfor %}

## Goals

- Minimal complete and secure code examples for common crypto scenarios
- Using only **standard library functionality** where possible (minimal)
- Using only **secure cryptographic functionality** (secure)
- Providing **copyable code** that can be used right away (complete)
- Working with the **latest stable release of the programming language** or compiler
- Indicating **reviewed code**
- Automatic **unit tests** for all code examples
- State the cryptographic threats that are mitigated in each example
- State the cryptographic guarantees/features in each example

# Detailed Information

### Choosing secure values for key size/length

Tradeoff between speed and security. Usually a longer key is harder to guess by an attacker through a brute-force attack, but using a longer key also makes the computation take more time.

- [keylength.com](https://www.keylength.com/en/compare/)
- [eBACS (ECRYPT Benchmarking of Cryptographic Systems)](https://bench.cr.yp.to/)
