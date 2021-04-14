<template>
    <div style="align-self: center;">
        <el-row class="top_buttons">
            <el-button @click="openCreateFolderDialog" size="mini" type="primary" icon="el-icon-circle-plus" title="Add new space" circle></el-button>
            <el-button size="mini" type="info" icon="el-icon-more" title="Expand more" circle></el-button>
            <el-button size="mini" type="warning" icon="el-icon-search" title="Deep search in current folder" circle></el-button>
            <el-button @click="deleteCurrentNode" size="mini" type="danger" icon="el-icon-delete" title="Delete Selected nodes" circle></el-button>
        </el-row>
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
    </div>
</template>

<script>
    import {mapActions} from 'vuex'

    export default {
        name: "CurrentNodeActions",
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
                filterText: "",
                data: [],
                rootFolder: {},
                defaultProps: {
                    children: 'children',
                    label: 'name',
                    isLeaf : (data, node)=> data.file,
                    path: "path",
                    nodeKey: "id"
                },
                defaultExpandedKeys: []
            }
        },
        computed: {
            currentNode() {
                return this.$store.getters.getCurrentNode;
            },
            sortByFolderFirst(){
                return this.$store.getters.getSortByFolderFirst;
            },
        },
        watch: {
            filterText(val) {
                this.$refs.tree.filter(val);
            },
            currentNode(val) {
                if (val.id && this.defaultExpandedKeys.indexOf(val.id) === -1)
                    this.defaultExpandedKeys = [val.id];
            }
        },
        mounted() {
            let self = this;
            this.$bus.$on("file_uploaded", function (payLoad) {
            });

            this.$bus.$on("folder_created", function (payLoad) {
            });
        },
        methods: {
            ...mapActions([
                'getNodeByPath',
                'deleteNodeById',
                'getNodesByParentId',
                'createFolderNodeWithMetaData',
            ]),
            openCreateFolderDialog(){
                if(this.currentNode && (this.currentNode.folder|| this.currentNode.type==="FOLDER")){
                    this.nodeForm.path = this.currentNode.path;
                }else{
                    this.nodeForm.path = this.currentNode.parentPath || rootFolder.path;
                }
                //this.existingPaths = this.data.map(el=> el.path);
                console.log(_.flatten(this.data));
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
            deleteCurrentNode(){
                let self = this;
                this.$confirm(`Voulez vous supprimer '${this.currentNode.name}' ?`,'Warning', {
                    confirmButtonText: 'OK',
                    cancelButtonText: 'Annuler',
                    type: 'warning'
                })
                    .then(yes=>{
                        self.deleteNodeById(self.currentNode.id,true)
                            .then(count=>{
                                console.log(count);
                                this.$message({
                                    message: `'${this.currentNode.name}' supprimé avec succes`,
                                    type: 'success'
                                });
                                self.$bus.$emit("node_deleted",self.currentNode);
                                self.$store.commit("storeCurrentNode",{});
                            })
                            .catch(error=>{
                                console.error(error);
                                this.$message({
                                    message: error,
                                    type: 'error'
                                });
                            })
                    });
            },
            openFullScreen2() {
                const loading = this.$loading({
                    lock: true,
                    text: 'Loading',
                    spinner: 'el-icon-loading',
                    background: 'rgba(0, 0, 0, 0.7)'
                });
                setTimeout(() => {
                    loading.close();
                }, 2000);
            }
        }
    }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
    .top_buttons {
        display: flex;
        height: 30px !important;
        justify-content: start;
        padding: 5px 3px;
        background-color: #eaedf2;
        margin-bottom: 2px;
        border-radius: 2px;
    }

    .top_buttons >>> button, .top_buttons >>> button i {
        font-size: 10px;
    }

    .top_buttons >>> button {
        padding: 4px;
    }
    .search_input {
        height: 30px;
        margin-bottom: 5px;
    }

    .search_input >>> input, .search_input >>> i {
        height: 24px !important;
        line-height: 24px !important;
    }

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
