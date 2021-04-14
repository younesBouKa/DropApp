<template>
    <el-dialog
                title="Create new Folder"
                :visible.sync="dialogVisible"
                width="33%"
                :before-close="handleCloseFolderCreationDialog">
            <el-alert
                    v-show="errorAlert"
                    title="Erreur"
                    type="error"
                    show-icon
                    close-text="Got it"
                    :description="errorAlert"
            ></el-alert>
            <el-form :label-position="'top'" label-width="100px" :model="nodeForm" size="mini" :rules="rules" @submit.native.prevent>
                <el-form-item label="Name" prop="name">
                    <el-input v-model="nodeForm.name"></el-input>
                </el-form-item>
                <el-form-item label="Parent folder" prop="path">
                    <el-select
                            v-model="nodeForm.path"
                            filterable
                            allow-create
                            placeholder="Selectionner un chemin"
                            :autocomplete="'on'"
                            :no-data-text="'tapez le chemin pour l\'ajouter'"
                    >
                        <el-option
                                v-for="item in existingPaths"
                                :key="item.value"
                                :label="item.label"
                                :value="item.value">
                        </el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="Permission" prop="permission">
                    <el-radio-group v-model="nodeForm.permission">
                        <el-radio-button label="READ" name="type" border></el-radio-button>
                        <el-radio-button label="WRITE" name="type" border></el-radio-button>
                        <el-radio-button label="READ_WRITE" name="type" border></el-radio-button>
                    </el-radio-group>
                </el-form-item>
            </el-form>
            <span slot="footer" class="dialog-footer">
                <el-button @click="dialogVisible = false">Cancel</el-button>
                <el-button type="primary" @click="createFolder">Create</el-button>
            </span>
    </el-dialog>
</template>

<script>
    import {mapActions} from 'vuex'

    export default {
        name: "CreateFolderDialog",
        props: {
            msg: String
        },
        data() {
            return {
                rules : {
                    name : [
                        {required:true, message: "Veuillez indiquer un nom pour ton dossier", trigger: 'blur'}
                    ],
                    path:[
                        {required:true, message: "Veuillez indiquer ou voulez vous creer le dossier", trigger: 'blur'}
                    ]
                },
                nodeForm : {
                    name: "",
                    path: "",
                    permission: "",
                },
                existingPaths : [],
                errorAlert : undefined,
                dialogVisible: false,
                defaultProps: {
                    children: 'children',
                    label: 'name',
                    isLeaf : (data, node)=> data.file,
                    path: "path",
                    nodeKey: "id"
                }
            }
        },
        computed: {
            currentNode() {
                return this.$store.getters.getCurrentNode;
            },
        },
        watch: {
        },
        mounted() {
            let self = this;
            this.$bus.$on("create_folder", function (payLoad) {
                self.openCreateFolderDialog(payLoad);
            });
        },
        methods: {
            ...mapActions([
                'getNodeByPath',
                'deleteNodeById',
                'getNodesByParentId',
                'createFolderNodeWithMetaData',
            ]),
            openCreateFolderDialog(currentNode){
                if(!currentNode){
                    currentNode = this.currentNode;
                }
                if(currentNode && (currentNode.folder|| currentNode.type==="FOLDER")){
                    this.nodeForm.path = currentNode.path;
                }else{
                    this.nodeForm.path = currentNode.parentPath || "";
                }
                //this.existingPaths = this.data.map(el=> el.path);
                this.dialogVisible = true;
            },
            handleCloseFolderCreationDialog(done){
                console.log("handleCloseFolderCreationDialog", done);
                done();
            },
            createFolder(){
                let folderNameRegex = /^[A-Za-z0-9\s_-]+$/ig;
                let folderPathRegex = /^\/|(\/[a-zA-Z0-9_-]+)+$/ig;
                if(!folderNameRegex.test(this.nodeForm.name)){
                    this.errorAlert= "Folder name is invalid";
                    return;
                }
                if(!folderPathRegex.test(this.nodeForm.path)){
                    this.errorAlert= "Folder path is invalid";
                    return;
                }

                this.createFolderNodeWithMetaData(this.nodeForm)
                    .then(response=>{
                        console.log(response);
                        this.errorAlert = undefined;
                        this.$message({
                            message: 'Dossier "'+response.name+'" crée avec  success.',
                            type: 'success'
                        });
                        this.$bus.$emit("folder_created",response);
                        this.dialogVisible = false;
                    })
                    .catch(error=>{
                        console.error(error);
                        this.errorAlert = error;
                        this.$message({
                            message: error,
                            type: 'error'
                        });
                    })
            },
            getRootFolder() {
                let self = this;
                return new Promise((resolve, reject) => {
                    self.getNodeByPath("/")
                        .then(data => {
                            console.log(data);
                            resolve(data);
                        }).catch(err => {
                            console.error(err);
                            reject(err);
                        });
                });
            },
        }
    }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
    /* dialog folder form */
    .el-dialog__wrapper >>> .el-dialog__body {
        padding: 10px 15px;
    }

    .el-alert >>> .el-alert__content {
        display: flex;
        flex-direction: column;
        align-items: flex-start;
        padding: 0 8px;
    }

    .el-alert >>> .el-alert.is-light .el-alert__closebtn {
        color: unset;
    }

    .el-form >>> .el-form-item__label{
        padding: 0;
        float: left;
        font-size: 12px;
    }
</style>
