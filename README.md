# Elastish - Clojure client for ElasticSearch REST API

[![Continuous Integration status](https://secure.travis-ci.org/ifesdjeen/elastisch.png)](http://travis-ci.org/ifesdjeen/elastisch)


Basic reasoning behind that project is to create a good reference implementation for ES API, allow
people to use it with arbitrary DBs and use it in distributed / high performant systems.

# Why not wrap Java API?

Potentially you may get a performance gain wrapping Java API directly, or implementing your driver
in the same manner, since rest API uses it directly. Personally, I can't see that very useful, and
creating a wrapper could potentially make Elastish less Clojurish.

# This is hugely WIP

Until version 1.0.0 APIs may change any day. As soon as it's stable enough (subjectively), basic docs
and examples will be added and jar uploaded to Clojars.

## License

Copyright (C) 2011 Alex Petrov

Distributed under the Eclipse Public License, the same as Clojure.