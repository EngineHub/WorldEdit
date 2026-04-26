// "Publish" (only to local projects) a resources variant for other projects to consume
configurations.consumable("resourcesVariant") {
    // Similar to mainSourceElements
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category::class, Category.VERIFICATION))
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling::class, Bundling.EXTERNAL))
        attribute(VerificationType.VERIFICATION_TYPE_ATTRIBUTE, objects.named(VerificationType::class, "resources"))
    }
    outgoing.artifact(tasks.named("processResources"))
}
