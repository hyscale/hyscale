<table>
  <tr>
    <th width="24%" class="tg-0lax">Type of error (Error group)</th>
    <th width="65%" class="tg-cly1">Description</th>
    <th width="11%" class="tg-cly1">Exit code</th>
  </tr>
  <tr>
    <td>Service Spec processing failed</td>
    <td>Errors occurred during the hspec file processing.<br>
        Ex: 1. While parsing details of hspec file.<br>
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;2. Missing of any required field in spec file like name, ports etc.
    </td>
    <td>15</td>
  </tr>
  <tr>
        <td>Service profile processing failed</td>
        <td>Errors occurred during the hprof file processing.<br>
            Ex: While parsing details of hprof file.
        </td>
        <td>30</td>
  </tr>
  <tr>
        <td>Upfront validation failed</td>
        <td> Errors occurred during validation of predeployment activities required for the deployment process.<br>
             Ex: Error while connecting to the cluster details provided in $HOME/.kube/config
        </td>
        <td>45</td>
  </tr>
  <tr>
        <td>Dockerfile generation failed</td>
        <td> Errors occurred during the Docker file generation process.<br>
             Ex: Missing of artifacts mentioned in the buildspec module of hspec file.
        </td>
        <td>60</td>
  </tr>
  <tr>
        <td> Image build failed (Includes image tagging also) </td>
        <td> Errors occurred when pushing image to registry hub.<br>
             Ex: If stackImage mentioned in hspec is invalid.
        </td>
        <td>75</td>
  </tr>
  <tr>
        <td> Image push failed </td>
        <td> Errors occurred during the Docker image building process.<br>
             Ex:  Docker credentials missing in $HOME/.docker/config.json are invalid.
        </td>
        <td>90</td>
  </tr>
  <tr>
        <td> Manifest generation failed </td>
        <td> Errors occurred during Kubernetes manifest generation process.<br>
             Ex:  If cpuThreshold or size mentioned in hspec is in invalid format.
        </td>
        <td>105</td>
  </tr>
  <tr>
        <td> Deployer apply failed </td>
        <td> Errors occurred while triggering the deployment process.<br>
             Ex:   while trying to create a resource based on namespace.
        </td>
        <td>120</td>
  </tr>
  <tr>
        <td> Wait for deployment failed </td>
        <td> Errors occurred during the deployment waiting stage.<br>
             Ex: 1. Issue in pod creation or pod initialization.<br>
             &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;2. Issue in pod readiness stage.
        </td>
        <td>135</td>
  </tr>
  <tr>
        <td>Service IP retrieval failed </td>
        <td>Error occurred while trying to get the IP address of the deployed service.</td>
        <td>201</td>
  </tr>
  <tr>
        <td>Get Kubernetes APIClient failed</td>
        <td>Error while communicating with Kubernetes client via API using the details mentioned in $HOME/.kube/config</td>
        <td>202</td>
  </tr>
  <tr>
        <td>Logs retrieval failed </td>
        <td>Errors while trying get logs from pod.</td>
        <td>203</td>
  </tr>
</table>
