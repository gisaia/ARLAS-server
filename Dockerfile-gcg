FROM ruby:2.4-alpine3.7

#ENV GITHUB_CHANGELOG_GENERATOR_VERSION ${GCG_VERSION}

RUN gem install github_changelog_generator --version 1.15.0.pre.rc

ENV SRC_PATH /usr/local/src/your-app
RUN mkdir -p $SRC_PATH
VOLUME [ "$SRC_PATH" ]
WORKDIR $SRC_PATH

CMD ["--help"]
ENTRYPOINT ["github_changelog_generator"]