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
            :limit="3"
            :on-exceed="handleExceed"
            :file-list="fileList"
            multiple>
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">Déposer les fichiers ici ou<em>&nbsp;cliquez pour envoyer</em></div>
    </el-upload>
</template>

<script>
    export default {
        name: "UploadFiles",
        components: {
        },
        data() {
            return {
                uploadUrl : "/api/file/uploadFile",
                fileList: [],
                data : {"toto":"test"}
            }
        },
        methods: {
            beforeUpload(file) {
                console.log(`beforeUpload : `,file);
                return this.$confirm(`Upload file : ${file.name} ?`, "Warning", {
                    confirmButtonText: 'OK',
                    cancelButtonText: 'Annuler',
                    type: 'warning'
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