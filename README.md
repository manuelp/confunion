# confunion

Confunion is a library to manage configuration files.

## Features

* Clojure and Java APIs.
* Merging of multiple configuration files.
* Supports *schemas*, for documentation and validation.
* Configurations and schemas are written in [EDN](http://edn-format.org) format.
* Informative error messages.
* Import/overwrite configuration in `java.util.Properties` objects (for interoperability purposes).

## Changelog

### 0.2.1
* Moved source and target to JVM 1.7
* Upgraded dependencies:
    * clojure 1.8.0
    * tools.logging 0.3.1
* Failed validation exception contains human-readable error descriptions.

### 0.2.0

Added basic typing support to schemas.

### 0.1.0

First feature-complete version. Supports multiple configuration files and a schema with cross-validations.

## Motivation

I needed a way to easily manage environment-specific configuration files for an application that is installed in multiple heterogeneous environments: development machines, QA servers, pre-production and production installations for several customers, etc. In short, *I need a way to load and validate against a schema an EDN map by merging the content of a base file with an (optional) override one, both read from a set of possible paths in which to find them*. The schema is particularly important in my case for documentation an validation purposes (so that I can keep the application and various environment-specific configurations in sync).

So, my main requirements were:

* It has to be file-based. Preferably in EDN format, so I can have a semantically rich but also general configuration format.
* A configuration should be built by merging multiple EDN maps read from a set of possible locations (configurable).
* It should validate the resulting configuration against a schema that describes the properties it should have.
* It should *not* be stateful, how this map is used should not be a concern for this library.

I have not found an existing library that satisfies all of my requirements. For example:

* [Environ](https://github.com/weavejester/environ) it's based on environment variables and system properties.
* [Nomad](https://github.com/james-henderson/nomad) is based around the concept of hostname-based configuration, which doesn't fit so well with the use cases I had in mind.
* [clonfig](https://github.com/mccraigmccraig/clonfig) is environment variables only.
* [conf-er](https://github.com/TouchType/conf-er) is based on a single file.

Plus, no existing library (that I'm aware of) supports schemas, a requirement that for me is key.

My quick analysis is probably incomplete, so if you think your library fits my use case then let me know and let's join forces!

## Project Maturity

As of today it should be considered *alpha quality*.

## Supported Clojure/Java versions

Confunion has been tested with Clojure 1.6 and JDK 1.7, however it should also work with Clojure 1.5 and JDK 1.6.

## installation

Artifacts are released to [Clojars](https://clojars.org/me.manuelp/confunion).

### Leiningen ###

Add dependency in your *project.clj*:

[![Current Version](https://clojars.org/me.manuelp/confunion/latest-version.svg)](https://clojars.org/me.manuelp/confunion)

### Maven ###

Add Clojars repository definition to your *pom.xml*:

```xml
<repository>
  <id>clojars.org</id>
  <url>http://clojars.org/repo</url>
</repository>
```

and then the dependency:

```xml
<dependency>
  <groupId>me.manuelp</groupId>
  <artifactId>confunion</artifactId>
  <version><!-- x.y.z --></version>
</dependency>
```

## Usage

The surface of this library is quite small, basically there are only two entry points:

* Configuration loading and validation.
* `java.util.Properties` writing.

### Schema Format

A schema is defined by an EDN data structure (see [here](https://github.com/wagjo/serialization-formats) for a comparison between EDN and JSON): a vector of maps, each one of them is a description of a property that a configuration map should (or may) have. Every *parameter description* should have three properties:

* `:schema/param`: the keyword of the parameter (it's name, or code if you want).
* `:schema/doc`: a documentation string (which is useful both for documentation and useful error messages).
* `:schema/mandatory`: a boolean that indicates if the described parameter is mandatory or not.
* `:schema/type`: type of the valid parameter values. One of:
  * `:schema.type/string`
  * `:schema.type/boolean`
  * `:schema.type/number`
  * `:schema.type/any` (this is a catch-all, to use *only* if none of the existing keys describe the value type)

All this *parameter description* entries are mandatory.

The schema currently is only used to verify if all entries are documented and all mandatory ones are present.

A simple example:

```clojure
[{:schema/param :a
  :schema/doc "A very useful configuration"
  :schema/mandatory false
  :schema/type :schema.type/string}
 {:schema/param :b
  :schema/doc "Some other useful configuration parameter."
  :schema/mandatory true
  :schema/type :schema.type/boolean}]
```

### Configuration Map Format

It's a standard EDN map. An example:

```clojure
{
  :a 1
  :b [2 "hello" "Sarah"]
  :c {
       :key "value"
       :another [21 71]
     }
  :d #{"A" "B"}
}
```

### Clojure

The higher-level function is:

```clojure
(require '[confunion.core :as conf])

(conf/load-configuration "path/to/schema.edn"
                         ["paths/to/possible.edn" "base/configuration/files.edn"]
                         ["overrides/files.edn" "/paths/to/inspect.edn"])
;; Returns the merged and validated map, or an exception with a detailed
;; message.
```

This is good if you want to merge the content of a base file and an optional overrides one, both to be searched in an ordered sequence of paths.

If you want to write a generic EDN map into a `java.util.Properties` object, you can use:

```clojure
(require '[confunion.properties :as props])

(props/add-properties (new java.util.Properties)
                      configuration-map)
```

### Java

The Java API is a mirror of the Clojure one (only much more verbose). You only need instantiate and use a `Confunion` object:

```java
Confunion cu = new Confunion();
Map configurationMap = cu.loadConfiguration("path/to/schema.edn",
                                  new ArrayList<String>() {
                                    {
                                      add("paths/to/possible.edn");
                                      add("base/configuration/files.edn");
                                    }
                                  },
                                  new ArrayList<String>() {
                                    {
                                      add("overrides/files.edn");
                                      add("/paths/to/inspect.edn");
                                    }
                                  });

cu.addProperties(new Properties(), configurationMap);
```

There is also a lower-level API accessible through the `ConfunionEngine` class.

## Documentation

You can generate a *literate-like* export of this project sources by using [Marginalia](https://github.com/gdeer81/marginalia). Basically (assuming you already have [Leiningen](http://leiningen.org/) installed) you just need to run this command:

```
lein marg
```

And open the resulting *docs/uberdoc.html* file with a web browser.

## License

Copyright © 2014 Manuel Paccagnella

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
