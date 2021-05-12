<template>
    <div class="sign">
        <el-container style="margin-left: 5px;height: 500px;">
        <el-header >
            <el-row class="el-header-row">
                <el-col class="header-left-part">
                </el-col>
                <el-col class="header-right-part">
                </el-col>
            </el-row>
        </el-header>

        <el-main>
            <el-form :label-position="labelPosition" label-width="100px" :model="signForm">
                <el-form-item label="Username">
                    <el-input v-model="signForm.username"></el-input>
                </el-form-item>
                <el-form-item label="Email" v-show="!isLogin">
                    <el-input type="email" v-model="signForm.email"></el-input>
                </el-form-item>
                <el-form-item label="Password">
                    <el-input type="password" v-model="signForm.password"></el-input>
                </el-form-item>
                <el-form-item label="Confirm Password" v-show="!isLogin">
                    <el-input type="password" v-model="signForm.confirmPassword"></el-input>
                </el-form-item>
                <el-form-item label="Birthday" v-show="!isLogin">
                    <el-input type="date" v-model="signForm.birthday"></el-input>
                </el-form-item>

                <el-form-item>
                    <label>Create new account or login?</label>
                    <el-switch v-model="isLogin" active-color="#13ce66" inactive-color="#ff4949"></el-switch>
                </el-form-item>

                <el-form-item>
                    <el-button type="primary" @click="submitForm">
                        {{isLogin ? 'Login' : 'Create account'}}
                    </el-button>
                </el-form-item>
            </el-form>
        </el-main>
    </el-container>
    </div>
</template>

<script>
    // @ is an alias to /src
    import UploadFiles from '@/components/UploadFiles.vue'
    import SpaceTree from '@/components/SpaceTree.vue'
    import FolderContent from '@/components/FolderContent.vue'
    import CreateFolderDialog from '@/components/CreateFolderDialog.vue'

    import { mapActions,mapGetters } from 'vuex'
    export default {
        name: 'Sign',
        components: {
        },
        data() {
            return {
                rules : {
                    username : [
                        {required:true, message: "Veuillez indiquer une nom d'utilisateur", trigger: 'blur'}
                    ],
                    password:[
                        {required:true, message: "Veuillez indiquer le mot de passe", trigger: 'blur'}
                    ]
                },
                isLogin : true,
                signForm : {
                    username: "",
                    password: "",
                    confirmPassword: "",
                    email: "",
                    birthday : "",
                    roles : [],
                },
                labelPosition : "top",
            }
        },
        computed : {

        },
        methods: {
            submitForm(){
                console.log("submit sign Form : ",this.signForm);
                if(!this.validate())
                    return;
                this.isLogin ? this.login(): this.register();
            },
            validate(){
              console.log("validate sign form: ",this.signForm);
              return true;
            },
            register(){
                console.log("create account : ",this.signForm);
                if(!this.validate())
                    return;
                let self = this;
                let data = this.signForm;
                this.$store.dispatch("register",data)
                    .then(response => {
                        this.$message({
                            message: 'Account created width success.',
                            type: 'success'
                        });
                        self.isLogin = true;
                    })
                    .catch(error => {
                        this.$message({
                            message: error,
                            type: 'error'
                        });
                    });
            },
            login(){
                console.log("login with: ",this.signForm);
                if(!this.validate())
                    return;
                let self = this;
                let type = this.isLogin ? "login" : "register";
                let data =  {username: this.signForm.username, password: this.signForm.password};
                this.$store.dispatch("login",data)
                    .then(response => {
                        self.$store.commit("setUser", response);
                        this.$message({
                            message: 'logged width success.',
                            type: 'success'
                        });
                        this.$router.push('/home');
                    })
                    .catch(error => {
                        this.$message({
                            message: error,
                            type: 'error'
                        });
                });
            }
        },
        mounted() {

        }
    }
</script>

<style>

    .el-header-row{
        display: flex;
        flex-direction: row;
    }

    .header-left-part{
        display: flex;
        flex-direction: row;
        justify-content: flex-start;
    }

    .header-right-part{
        display: flex;
        flex-direction: row;
        justify-content: flex-end;
    }

    .el-form{
        display: flex;
        flex-direction: column;
        width: 30%;
        margin: auto;
    }

    /******* scroll bar *************/

    ::-webkit-scrollbar {
        width: 5px;
    }

    /* Track */
    ::-webkit-scrollbar-track {
        background: none;
    }

    /* Handle */
    ::-webkit-scrollbar-thumb {
        background: none;
    }

    /* Handle on hover */
    ::-webkit-scrollbar-thumb:hover {
        background: #cfc8c8;
        width: 10px;
    }
</style>