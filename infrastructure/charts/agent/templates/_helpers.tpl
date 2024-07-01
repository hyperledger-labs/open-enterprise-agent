{{/*
Expand the name of the chart.
*/}}
{{- define "cloud-agent.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "cloud-agent.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "cloud-agent.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "labels.common" -}}
helm.sh/chart: {{ include "cloud-agent.chart" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/part-of: {{ include "cloud-agent.fullname" . }}
{{- end }}





{{- define "cors" }}
    {{- if .Values.ingress.cors.enabled }}
    - name: cors
      enable: true
      config:
        allow_origins:
        {{- if .Values.ingress.cors.allow_origins }}
          {{ .Values.ingress.cors.allow_origins | quote }}
        {{- else }}
          {{ printf "https://%s" (index .Values.ingress.applicationUrls 0) | quote }}
        {{- end }}
    {{- end }}
{{- end -}}

{{- define "consumer-restriction" }}
    - name: consumer-restriction
      enable: {{ .Values.ingress.auth.consumer_restriction }}
      config:
        whitelist:
        {{- range .Values.ingress.auth.consumers }}
          -  {{ regexReplaceAll "-" $.Release.Name "_" }}_{{ regexReplaceAll "-" . "_" | lower }}
        {{- end }}
        {{- range .Values.ingress.auth.externalConsumers }}
          -  {{ regexReplaceAll "-" $.Release.Name "_" }}_{{ regexReplaceAll "-" . "_" | lower }}
        {{- end }}
{{- end -}}


{{- define "headers.security" }}
    - name: response-rewrite
      enable: true
      config:
        headers:
          set:
            X-Content-Type-Options: "nosniff"
            X-Frame-Options: "deny"
            Content-Security-Policy: "default-src 'self' data:; script-src 'self'; connect-src 'self'; img-src 'self' data:; style-src 'self'; frame-ancestors 'self'; form-action 'self';"
            Strict-Transport-Security: "max-age=31536000; includeSubDomains"
            Referrer-Policy: "same-origin"
            Cache-Control: "no-cache, no-store"
          remove: ["Server"]
{{- end -}}

{{- define "headers.requestId" }}
    - name: request-id
      enable: true
      config:
        header_name: "X-Request-ID"
        include_in_response: true
{{- end -}}
