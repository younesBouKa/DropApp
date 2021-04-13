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
            :limit="3"
            :on-exceed="handleExceed"
            :file-list="fileList"
            :show-file-list="false"
            multiple>
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">Déposer les fichiers ici ou<em>&nbsp;cliquez pour envoyer</em></div>
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
                uploadUrl : "/api/file/uploadFile",
                fileList: [],
                data : {"toto":"test"},
                loading : undefined,
            }
        },
        computed: {
            selectedNode(){
                return this.$store.getters.getCurrentNode;
            },
            isSelectedNodeFolder(){
                return this.selectedNode.folder;
            },
            isEmptyFolder(){
                return this.isSelectedNodeFolder && this.data.length===0;
            },
            canUploadFileInFolder(){
                return this.isSelectedNodeFolder && this.isEmptyFolder;
            },
            /* ...mapGetters({
                 selectedNode:"getCurrentNode"
             }),*/
        },
        methods: {
            beforeUpload(file) {
                console.log(`beforeUpload : `,file);
                const self = this;
                return new Promise((resolve, reject) => {
                    this.data = {};
                    if(this.isSelectedNodeFolder)
                        this.data.parentId = this.selectedNode.id;
                    else
                        this.data.parentId = this.selectedNode.parentId;
                    this.$confirm(`Upload file : ${file.name} ?`, "Warning", {
                        confirmButtonText: 'OK',
                        cancelButtonText: 'Annuler',
                        type: 'warning'
                    }).then(yes =>{
                        console.log(yes);
                        self.loading = this.$loading({
                            lock: true,
                            text: 'Loading',
                            spinner: 'el-icon-loading',
                            background: 'rgba(0, 0, 0, 0.7)'
                        });
                        resolve();
                    }).catch(no=>{
                        console.log(no);
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
                this.$message.warning(`La limite est 3, vous avez choisi ${files.length} fichiers, soit ${files.length + fileList.length} au total.`);
            },
            beforeRemove(file, fileList) {
                return this.$confirm(`Supprimer le transfert de ${file.name} ?`,'Warning', {
                    confirmButtonText: 'OK',
                    cancelButtonText: 'Annuler',
                    type: 'warning'
                });
            },
            onProgress(event, file, fileList){
                console.log(`onProgress :`,event,file,fileList);
            },
            onSuccess(response, file, fileList){
                console.log(`onSuccess :`,response,file,fileList);
                if(this.loading)
                    this.loading.close();
                this.$bus.$emit("file_uploaded",response);
            },
            onError(err, file, fileList){
                console.log(`onError :`,err,file,fileList);
                if(this.loading)
                    this.loading.close();
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