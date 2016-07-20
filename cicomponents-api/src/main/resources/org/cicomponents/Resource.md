# Resource

Resource interface is a very simple building block in CI Components. Basically, it is something that can be acquired and released. Typically, `#acquire()` and `#release()` will employ some tracking mechanism, like an atomic counter with a cleanup release procedure when the last acquirer releases the resource.

An example of a resource would be a checked out git revision that needs to be removed from disk once all interested parties have processed it.
