# Single source of truth for frontend Node/npm versions is frontend/pom.xml
# (<frontend.node.version>, <frontend.npm.version>). Keep this build stage in sync with it.
ARG NODE_VERSION=22.23.2
FROM node:${NODE_VERSION}-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

ARG NGINX_VERSION=1.27-alpine
FROM nginx:${NGINX_VERSION}
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
