{{- define "cors" }}
    {{- if .Values.ingress.cors.enabled }}
    - name: cors
      enable: true
      {{- if .Values.ingress.cors.allow_origins }}
      config:
        allow_origins: {{ .Values.ingress.cors.allow_origins | quote }}
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
{{- define "labels.common" -}}
app.kubernetes.io/part-of: {{ .Chart.Name }}
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
