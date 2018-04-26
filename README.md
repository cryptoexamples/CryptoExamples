[![Build Status](https://travis-ci.org/kmindi/crypto-examples.svg?branch=master)](https://travis-ci.org/kmindi/crypto-examples)

# Introduction

Many applications need cryptography. There are many examples in the web, that are either insecure or do not work right away. 
As programmers are usually not cryptography or security experts, they should be able to take the path of least resistance and not have to bother with all the decisions needed to make cryptography really secure.
The **crypto examples provided on this site meet current security and cryptography requirements**.
They demonstrate how cryptography can be used in many programming languages for common use cases like encrypting a String or a file using symmetric or asymmetric encryption. 

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

## Available examples
### Java
#### JDK

- [kmindi/java-crypto-examples](https://github.com/kmindi/java-crypto-examples)

#### [Keyczar](https://github.com/google/keyczar)

- [kmindi/java-keyczar-crypto-examples](https://github.com/kmindi/java-keyczar-crypto-examples)

# More about CryptoExamples
## Architecture
![Target Architecture](images/architecture.png)

Actor | Description
--- | ---
Project Owner | Has owner rights of any given repositories, therefore he can decide who becomes an expert. By now this is restricted to the founder of CryptoExamples, [M.Sc. Kai Mindermann](https://github.com/kmindi)
Expert | Masters a given library and knows it best. Is allowed to accept pull requests made by contributors. To become an expert, feel free to contact any project owner and throw in some words about you and your work or even link to it.
Contributor | Maybe you? Feel free to contribute awesome security examples in any field you feel comfortable with! Below you will see how easy it is to contribute.

## How to contribute
There are two thinkable ways of contributing code. Either is it by contributing code to an already existing language/library, or by contributing an example to a completely new language/library.
Each library has its own submodule which includes all necessary files for any changes. Therefore you do not need to clone the whole library, it's okay to just clone the corresponding submodule/repository.

### (a) Contributing code to an existing language/library
1. Fork the repository corresponding to the library you wish to contribute code to.
2. Update the repository with all the code you want to submit.
  * Feel free to either update existing examples or to submit your owns.
3. Submit a pull request in the origin repository which you forked.
  * An expert will check all changes and either accept or decline your pull request.
  * Before accepting the pull request, it might be that the expert requests you to update a few things before accepting.
  
### (b) Contributing code with a not yet into CryptoExamples integrated language/library
1. Fork the [template project](https://github.com/cryptoexamples/template-java-crypto-examples)
2. Update it the way that it fits the needs of the language/library
  * Feel free to update its structure as different languages use different folder structures
3. Open a issue in this main repository, linking to your project.
  * You might need to revise it if the project owner(s) might be missing something.
  * The project owner(s) will then include a copy of your project into CryptoExamples.
4. As soon as a copy of your project has been added to CryptoExamples, feel free to contribute any (more) code as in (a)

# Other

Theme based on http://idratherbewriting.com/documentation-theme-jekyll/
