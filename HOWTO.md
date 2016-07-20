# Developing a CI

Unlike many other CI projects, CI Components doesn't try to fit everything into one vision of how things should be organized — be it projects, build steps, pipelines, or whatever. Instead, it provides a number of components that are useful in building a CI. Therefore, your project's CI is just a little bit of code, written in Java or any other OSGi-compatible JVM language.

Every CI is an OSGi bundle (or feature) that implements components that facilitate the CI process. The best available example at this moment is [CI Components' own CI](https://github.com/cicomponents/cicomponents/tree/master/cicomponents-ci). Currently, it has a `Build` component that establishes `master` and pull requests listeners and runs Gradle tasks in those. It also has `GithubApplication` component that provides API keys for GitHub OAuth integration.
