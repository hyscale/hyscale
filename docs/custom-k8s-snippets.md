# Custom K8s Snippets in Hyscale
#### Description

Hyscale generates K8s manifest yamls with respect to Hspec that satisfies the majority of application use cases with exception to few kubernetes concepts.The thought behind Custom K8s Snippets is to eliminate the effect of current/temporary limitations of Hyscale over user’s K8s requirements and approach regarding application use cases.

Allowing the end user to attach their own k8s yaml snippets to the Hspec provides a choice to customize or override on top of generated manifests by Hyscale. Also ensuring that the usage of Hspec and deploy using Hyscale should not restrict an end user from utilizing any K8s Features which are yet to be abstracted by Hyscale.
