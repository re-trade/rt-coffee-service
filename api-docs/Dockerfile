FROM node:18 AS build
WORKDIR /app-build
COPY . .
RUN npm ci && npm run build

FROM nginx:alpine AS deploy
COPY --from=build /app-build/nginx.conf /etc/nginx/conf.d/default.conf
WORKDIR /usr/share/nginx/html
RUN rm -rf ./*
COPY --from=build /app-build/build .
ENTRYPOINT ["nginx", "-g", "daemon off;"]
