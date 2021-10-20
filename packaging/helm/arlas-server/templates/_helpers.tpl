{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "arlas-server.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "arlas-server.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- if contains $name .Release.Name -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "arlas-server.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "arlas-server.environmentVariables" -}}
    {{- range $key,$value := .Values.environmentVariables }}
- name: {{ $key | quote }}
  value: {{ $value | quote }}
    {{- end -}}
{{- end -}}


{{- define "arlas-server.imagePullSecrets" -}}
    {{- with .Values.imagePullSecrets -}}
imagePullSecrets:
{{ toYaml .  | indent 2 }}
    {{- end }}
{{- end }}


{{- define "arlas-server.namespace" -}}
    {{- with .Values.namespace -}}
namespace: {{ . | quote }}
    {{- end -}}
{{- end -}}


{{- define "arlas-server.revisionHistoryLimit" -}}
    {{- with .Values.revisionHistoryLimit}}
revisionHistoryLimit: {{ int . }}
    {{- end -}}
{{- end -}}


{{- define "arlas-server.service.labels" -}}
    {{- with .Values.service.labels -}}
        {{- range $key,$value := . }}
{{ $key }}: {{ $value | quote }}
        {{- end -}}
    {{- end -}}
{{- end -}}
