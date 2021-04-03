// vue.config.js
module.exports = {
    // proxy all webpack dev-server requests starting with /api
    // to our Spring Boot backend (localhost:8098) using http-proxy-middleware
    // see https://cli.vuejs.org/config/#devserver-proxy
    devServer: {
        port: process.env.DEV_PORT || 8080,
        proxy: {
            '/api': {
                target: process.env.SERVER_API || 'http://localhost:8080',
                ws: true,
                changeOrigin: true
            }
        }
    },
    // Change build paths to make them Maven compatible
    // see https://cli.vuejs.org/config/
    outputDir: 'target/dist',
    assetsDir: 'static'
}
