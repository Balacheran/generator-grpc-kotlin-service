const Generator = require('yeoman-generator');
const chalk = require('chalk');
const yosay = require('yosay');
const mkdirp = require('mkdirp');

const GrpcKotlinGenerator = class extends Generator {
  constructor(args, opts) {
    super(args, opts);
  }

  initializing() {
    this.log(yosay(
      'Welcome to the ' + chalk.red('gRPC Kotlin Service') + ' generator!'
    ));
  }

  async prompting() {
    this.answers = await this.prompt([
      {
        type: 'input',
        name: 'projectName',
        message: 'What is your project name?',
        default: this.appname.replace(/\s+/g, '-')
      },
      {
        type: 'input',
        name: 'packageName',
        message: 'What is your base package name?',
        default: 'com.example.grpc'
      },
      {
        type: 'confirm',
        name: 'includeFirebase',
        message: 'Would you like to include Firebase Authentication?',
        default: false
      }
    ]);
  }

  writing() {
    const packagePath = this.answers.packageName.replace(/\./g, '/');

    // Create project structure
    this._createDirectories(packagePath);
    
    // Copy template files
    this._copyTemplateFiles(packagePath);
    
    // Copy binary files
    this._copyBinaryFiles();
  }

  _createDirectories(packagePath) {
    const dirs = [
      `src/main/kotlin/${packagePath}`,
      'src/main/proto',
      'src/main/resources',
      `src/test/kotlin/${packagePath}`,
      'src/test/resources',
      'gradle/wrapper'
    ];

    dirs.forEach(dir => {
      mkdirp.sync(this.destinationPath(dir));
    });
  }

  _copyTemplateFiles(packagePath) {
    // Copy build files
    const buildFiles = [
      'build.gradle.kts',
      'settings.gradle.kts',
      'gradle.properties'
    ];

    buildFiles.forEach(file => {
      this.fs.copyTpl(
        this.templatePath(file),
        this.destinationPath(file),
        this.answers
      );
    });

    // Copy source files
    this.fs.copyTpl(
      this.templatePath('src/main/proto/service.proto'),
      this.destinationPath('src/main/proto/service.proto'),
      this.answers
    );

    this.fs.copyTpl(
      this.templatePath('src/main/kotlin/Server.kt'),
      this.destinationPath(`src/main/kotlin/${packagePath}/Server.kt`),
      this.answers
    );

     // Copy Client.kt
     this.fs.copyTpl(
      this.templatePath('src/main/kotlin/Client.kt'),
      this.destinationPath(`src/main/kotlin/${packagePath}/Client.kt`),
      this.answers
    );

    // Copy gradle wrapper properties
    this.fs.copyTpl(
      this.templatePath('gradle/wrapper/gradle-wrapper.properties'),
      this.destinationPath('gradle/wrapper/gradle-wrapper.properties'),
      this.answers
    );
  }

  _copyBinaryFiles() {
    // Copy gradle wrapper jar (binary)
    this.fs.copy(
      this.templatePath('gradle/wrapper/gradle-wrapper.jar'),
      this.destinationPath('gradle/wrapper/gradle-wrapper.jar')
    );

    // Copy gradle wrapper scripts
    this.fs.copy(
      this.templatePath('gradlew'),
      this.destinationPath('gradlew')
    );

    this.fs.copy(
      this.templatePath('gradlew.bat'),
      this.destinationPath('gradlew.bat')
    );
  }

  install() {
    // Make gradlew executable
    if (process.platform !== 'win32') {
      this.spawnCommand('chmod', ['+x', 'gradlew']);
    }
    
    this.log(chalk.green('\nProject generated successfully!'));
    this.log('\nTo get started:');
    this.log(chalk.cyan('\n  ./gradlew build'));
    this.log(chalk.cyan('  ./gradlew run'));
  }
};

module.exports = GrpcKotlinGenerator;