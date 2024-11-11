const publishCmd = `
./gradlew uploadKotlinOSSRHToMavenCentralNexus uploadPluginMavenToMavenCentralNexus uploadPluginMarkerMavenToMavenCentralNexus release || exit 1
./gradlew publishPlugins -Pgradle.publish.key=$GRADLE_PUBLISH_KEY -Pgradle.publish.secret=$GRADLE_PUBLISH_SECRET || exit 2
./gradlew publishKotlinOSSRHPublicationToGithubRepository publishPluginMavenPublicationToGithubRepository || true
`;
import config from 'semantic-release-preconfigured-conventional-commits' with { type: "json" };
config.plugins.push(
    [
        "@semantic-release/exec",
        {
            "publishCmd": publishCmd,
        }
    ],
    "@semantic-release/github",
    "@semantic-release/git",
)
export default config;
