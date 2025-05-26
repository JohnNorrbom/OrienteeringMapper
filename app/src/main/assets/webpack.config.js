const path = require('path');
const webpack = require('webpack');

module.exports = {
  mode: 'production',
  target: 'web',
  entry: './src/index.js',
  output: {
    filename: 'bundle.js',
    path: path.resolve(__dirname),
  },
  resolve: {
    fallback: { fs: false, buffer: require.resolve('buffer/') },
  },
  module: {
    rules: [
      {
        test: /\.worker\.js$/,
        use: {
          loader: 'worker-loader',
          options: { filename: 'worker.js', esModule: false }
        }
      }
    ]
  },
  plugins: [
    new webpack.ProvidePlugin({ Buffer: ['buffer', 'Buffer'] }),
  ],
};
