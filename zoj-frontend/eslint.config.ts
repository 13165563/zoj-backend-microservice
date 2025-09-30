// 旧配置示例（ESLint v8）
module.exports = {
  extends: ['eslint:recommended'],
  resolvePluginsRelativeTo: __dirname,
  rulePaths: ['./custom-rules'],
  ignorePath: '.gitignore',
  extensions: ['.js', '.ts', '.vue']
};

// 新配置适配（ESLint v9）
export default [
  {
    files: ['**/*.js', '**/*.ts', '**/*.vue'],
    extends: ['eslint:recommended'],
    // 移除已废弃的选项
    // 使用 plugins 替代 rulePaths
    plugins: {
      // 自定义插件
    }
  }
];
