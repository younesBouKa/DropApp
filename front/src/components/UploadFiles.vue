<template>
    <el-upload
            class="upload-demo"
            drag
            :data="data"
            :action="uploadUrl"
            :on-preview="handlePreview"
            :on-remove="handleRemove"
            :before-upload="beforeUpload"
            :before-remove="beforeRemove"
            :on-progress="onProgress"
            :on-success="onSuccess"
            :on-error="onError"
            :limit="limitFiles"
            :on-exceed="handleExceed"
            :file-list="fileList"
            :show-file-list="false"
            multiple>
        <i style="color: #67c23a" class="el-icon-upload"></i>
        <div class="el-upload__text">Déposer les fichiers ici ou<em>&nbsp;cliquez pour choisir</em></div>
    </el-upload>
</template>

<script>
    import {mapActions, mapGetters} from 'vuex'
    export default {
        name: "UploadFiles",
        components: {
        },
        data() {
            return {
                uploadUrl : "/api/file/uploadMultiFiles",
                fileList: [],
                data : {"toto":"test"},
                loading : undefined,
                limitFiles: 3,
            }
        },
        computed: {
            currentNode(){
                return this.$store.getters.getCurrentNodeData;
            },
            isCurrentNodeFolder(){
                return this.currentNode.folder;
            },
            isEmptyFolder(){
                return this.isCurrentNodeFolder && this.data.length===0;
            },
            canUploadFileInFolder(){
                return this.isCurrentNodeFolder && this.isEmptyFolder;
            },
        },
        methods: {
            beforeUpload(file) {
                console.log(`beforeUpload : `,file);
                const self = this;
                return new Promise((resolve, reject) => {
                    this.data = {};
                    if(this.isCurrentNodeFolder)
                        this.data.parentId = this.currentNode.id;
                    else
                        this.data.parentId = this.currentNode.parentId;
                    this.$confirm(`Upload file : '${file.name}' ?`, "Warning", {
                        confirmButtonText: 'OK',
                        cancelButtonText: 'Annuler',
                        type: 'warning'
                    }).then(yes =>{
                        self.loading = this.$loading({
                            lock: true,
                            text: 'Loading',
                            spinner: 'el-icon-loading',
                            background: 'rgba(0, 0, 0, 0.7)'
                        });
                        resolve();
                    }).catch(no=>{
                        reject();
                    });
                });
            },
            handleRemove(file, fileList) {
                console.log(`handleRemove : `, file, fileList);
            },
            handlePreview(file) {
                console.log(`handlePreview :`,file);
            },
            handleExceed(files, fileList) {
                this.$message.warning(`La limite est ${this.limitFiles}, vous avez choisi '${files.length}' fichiers, soit ${files.length + fileList.length} au total.`);
            },
            beforeRemove(file, fileList) {
                return true;
                /*this.$confirm(`Supprimer le transfert de '${file.name}' ?`,'Warning', {
                    confirmButtonText: 'OK',
                    cancelButtonText: 'Annuler',
                    type: 'warning'
                });*/
            },
            onProgress(event, file, fileList){
                console.log(`onProgress :`,event,file,fileList);
            },
            onSuccess(response, file, fileList){
                console.log(`onSuccess :`,response,file,fileList);
                if(this.loading)
                    this.loading.close();
                this.$message({
                    message: 'Fichier "'+response.name+'" téléchargé avec  success.',
                    type: 'success'
                });
                this.$bus.$emit("files_uploaded",response);
            },
            onError(error, file, fileList){
                console.log(`onError :`,error,file,fileList);
                if(this.loading)
                    this.loading.close();
                this.$message({
                    message: error,
                    type: 'error'
                });
            }
        },
        mounted() {
        }
    }
</script>

<style>
    /* upload element */

    .el-upload-dragger{
        width: 100% !important;
        height: 100% !important;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-direction: column;
    }

    .upload-demo{
        width: 100% !important;
        height: 100% !important;
    }

    .el-upload, .el-upload--text {
        width: 100% !important;
        height: 100% !important;
    }

    .el-icon-upload {
        vertical-align: middle;
    }


</style>