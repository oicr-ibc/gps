---
title: Studies | Heliotrope API
---

# Studies API

Management of studies via the API requires that you are
authenticated.

## List all studies

    GET /study

### Response

<%= headers 200 %>
<%= json({"identifier" => "GPS"}) %>

## Add a new study

    POST /study

### Input

You can post a single study descriptor:

<%= json({"identifier" => "GPS"}) %>

### Response

<%= headers 201 %>
<%= json({"identifier" => "GPS", "dateCreated" => "2011-01-26T19:06:43Z"}) %>

## Modify an existing study

    PUT /study/:study

### Input

You can specify study values:

<%= json({"identifier" => "NEW"}) %>

### Response

<%= headers 200 %>
<%= json({"identifier" => "NEW", "dateCreated" => "2011-01-26T19:06:43Z"}) %>

