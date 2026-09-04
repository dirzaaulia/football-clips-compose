if (config.devServer) {
    config.devServer.proxy = {
        '/football-crests': {
            target: 'https://crests.football-data.org',
            changeOrigin: true,
            pathRewrite: { '^/football-crests': '' },
            secure: false,
            onProxyReq: function(proxyReq, req, res) {
                proxyReq.setHeader('User-Agent', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36');
            }
        },
        '/youtube-images': {
            target: 'https://img.youtube.com',
            changeOrigin: true,
            pathRewrite: { '^/youtube-images': '' },
            secure: false
        },
        '/supabase': {
            target: 'https://eiomktvavndorazreyba.supabase.co',
            changeOrigin: true,
            pathRewrite: { '^/supabase': '' },
            secure: false
        }
    };
}
