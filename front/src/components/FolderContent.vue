<template>
    <div style="height: 100%">
        <el-row class="top-row">
            <el-col style="text-align: left;">
                <el-breadcrumb separator="/">
                    <el-breadcrumb-item
                            v-for="folder in pathFolders"
                            :disabled="!folder.id"
                            @click.native="setCurrentFolder(folder)"
                    >
                        {{folder.name}}
                    </el-breadcrumb-item>
                </el-breadcrumb>
            </el-col>
            <el-col class="folder_buttons_set">
                <div>
                    <el-button @click="goBackward" v-show="activateBackward" :disabled="!activateBackward" size="mini"  icon="el-icon-back" title="go Backward" circle></el-button>
                    <el-button @click="goForward" v-show="activateForward" :disabled="!activateForward" size="mini" icon="el-icon-right" title="go Forward" circle></el-button>
                    <el-divider direction="vertical" v-show="activateBackward || activateForward"></el-divider>

                    <el-button @click="enableUpload=!enableUpload" size="mini" :type="!enableUpload? 'success' : 'warning'" icon="el-icon-upload2" title="Upload file" circle></el-button>
                    <el-button @click="download" v-show="selectedNodes.length>0" :disabled="selectedNodes.length===0" size="mini" icon="el-icon-download" title="Download" circle></el-button>
                    <el-button @click="openCreateFolderDialog" type="primary" size="mini" icon="el-icon-folder-add" title="Create folder" circle></el-button>
                    <el-divider direction="vertical"></el-divider>

                    <el-button @click="pasteNodes" v-show="nodesInClipBoard.length>0" :disabled="nodesInClipBoard.length===0" size="mini" icon="el-icon-receiving" title="Coller" circle></el-button>
                    <el-button @click="moveNodes('CUT')" v-show="selectedNodes.length>0" :disabled="selectedNodes.length===0" size="mini" type="warning" icon="el-icon-scissors" title="Couper" circle></el-button>
                    <el-button @click="moveNodes" v-show="selectedNodes.length>0" :disabled="selectedNodes.length===0" size="mini" type="info" icon="el-icon-document-copy" title="Copier" circle></el-button>
                    <el-button @click="deleteNodes" v-show="selectedNodes.length>0" size="mini" type="danger" icon="el-icon-delete" title="Delete Selected nodes" circle></el-button>
                    <el-divider direction="vertical" v-show="nodesInClipBoard.length>0 || selectedNodes.length>0"></el-divider>

                    <el-button @click="isListView=!isListView" size="mini"  :icon="isListView?'el-icon-files':'el-icon-s-grid'" title="Switch views" circle></el-button>
                    <el-button @click="loadFolderContent" size="mini" type="info" icon="el-icon-refresh-right" title="Refresh" circle></el-button>
                    <el-button size="mini" v-show="false" type="warning" icon="el-icon-search" title="Search" circle></el-button>
                </div>
            </el-col>
        </el-row>
        <el-row class="content" >
            <UploadFiles v-if="canUploadFileInFolder"></UploadFiles>
            <TableView v-else-if="isListView && isCurrentNodeFolder" @selectionChanged="selectionChanged" :rows="data" :columns="columnsToShow"></TableView>
            <CardsView v-else-if="isCurrentNodeFolder" @selectionChanged="selectionChanged" :data="dataRows"></CardsView>
        </el-row>
        <el-row class="footer-row">
        </el-row>
    </div>
</template>

<script>
    import {mapActions, mapGetters} from 'vuex'
    import UploadFiles from '@/components/UploadFiles.vue'
    import CardsView from '@/components/CardsView.vue'
    import TableView from '@/components/TableView.vue'
    import JSZip from "jszip";
    const _ = require('lodash');
    export default {
        name: "FolderContent",
        components :{
            UploadFiles,
            CardsView,
            TableView
        },
        props: {
            folderId: {
                type: String,
                default: ()=> undefined
            },
        },
        data() {
            return {
                // switch between views
                isListView : false,
                foldersHistory : [],
                enableUpload : false,
                data: [],
                selectedNodes : [],
                defaultProps: {
                    children: 'children',
                    label: 'name'
                },
                columnsToShow : [
                    {
                        field : "name",
                        label : "Name",
                    },
                    {
                        field : "createDate",
                        label : "Date de creation",
                    },
                    {
                        field : "permission",
                        label : "Permission",
                    },
                    "type"
                ]
            }
        },
        computed: {
            currentNodeData(){
                return this.$store.getters.getCurrentNodeData;
            },
            isCurrentNodeFolder(){
               return this.currentNodeData.folder;
            },
            isFolderEmpty(){
               return this.isCurrentNodeFolder && this.data.length===0;
            },
            canUploadFileInFolder(){
                return this.enableUpload //|| this.isFolderEmpty;
            },
            permissionsOptions() {
                return this.$store.getters.getPermissionOptions;
            },
            sortByFolderFirst() {
                return this.$store.getters.getSortByFolderFirst;
            },
            dataRows() {
                let rows = this.data;
                // build permissions
                rows = rows.map(node => {
                    if (typeof node.permission === "string") {
                        let permissions = node.permission.split("_");
                        node.permission = [];
                        permissions.forEach(per => {
                            per = per.toLowerCase();
                            per = per === "" ? "none" : per;
                            node.permission.push(this.permissionsOptions[per]);
                        })
                    }
                    return node;
                });
                // sort
                return rows.sort(this.sortByFolderFirst);
            },
            currentNodeElement(){
                return this.$store.getters.getCurrentNodeElement;
            },
            pathFolders(){
                let allParents = [];
                if(this.currentNodeElement
                    && this.currentNodeElement.parent
                    && this.currentNodeData.id===this.currentNodeElement.data.id
                ){
                    let parent = this.currentNodeElement;
                    while (parent.parent){
                        let data = parent.data;
                        allParents.push(data);
                        parent = parent.parent;
                    }
                    allParents.reverse();
                }else if(this.currentNodeData &&  this.currentNodeData.path){
                    let paths =  this.currentNodeData.path.split("/");
                    allParents = paths.map(el=>{
                        return {
                          name : el,
                        };
                    })
                }
                return allParents ;
            },
            nodesInClipBoard(){
              return this.$store.getters.getNodesInClipBoard;
            },
            activateForward(){
                if(this.foldersHistory.length>0){
                    let index = this.foldersHistory.findIndex(elt=> elt.id === this.currentNodeData.id);
                    return index!==-1 && index< this.foldersHistory.length-1;
                }
                return false;
            },
            activateBackward(){
                if(this.foldersHistory.length>0){
                    let index = this.foldersHistory.findIndex(elt=> elt.id === this.currentNodeData.id);
                    return index!==-1 && index>0;
                }
                return false;
            }
           /* ...mapGetters({
                currentNodeData:"getCurrentNodeData"
            }),*/
        },
        watch: {
            currentNodeData(val) {
                this.loadFolderContent();
                this.selectedNodes=[];
                if(this.isCurrentNodeFolder){
                    let index = this.foldersHistory.findIndex(elt=> elt.id === this.currentNodeData.id);
                    if(index===-1)
                        this.foldersHistory.push(this.currentNodeData);
                }
            },
            folderId(val) {
                this.loadFolderContent();
            },
            visible(value) {
                if (value) {
                    document.body.addEventListener('click', this.closeMenu)
                } else {
                    document.body.removeEventListener('click', this.closeMenu)
                }
            }
        },
        mounted() {
            this.loadFolderContent();
            let self = this;
            this.$bus.$on("files_uploaded", function (payLoad) {
                (payLoad||[]).forEach(node=>{
                    if(node.parentId===self.currentNodeData.id){
                        self.data.push(node);
                    }
                })
                self.enableUpload=false;
            });
            this.$bus.$on("folder_created", function (payLoad) {
                if(payLoad.parentId===self.currentNodeData.id){
                    self.data.push(payLoad);
                }
            });
            this.$bus.$on("nodes_deleted", function (payLoad) {
                (payLoad||[]).forEach(node=>{
                    if(node.parentId===self.currentNodeData.id){
                        let index = self.data.findIndex(elt=> elt.id===node.id);
                        if(index!==-1)
                            self.data.splice(index,1);
                    }
                    let indexInHistory = self.foldersHistory.findIndex(elt=> elt.id === node.id);
                    if(indexInHistory!==-1)
                        self.foldersHistory.splice(indexInHistory,1);
                });
            });
        },
        methods: {
            ...mapActions({
                getNodesByParentId: 'getNodesByParentId',
                deleteNodesById: 'deleteNodesById',
            }),
            loadFolderContent() {
                console.log("loadFolderContent : "+this.currentNodeData);
                this.data = [];
                let folderId = this.folderId || this.currentNodeData.id;
                if (!folderId || folderId==="")
                    return;
                this.$store.dispatch("getNodesByParentId",folderId)
                    .then(nodes => {
                        console.log(nodes);
                        this.data = nodes;
                        this.selectedNodes=[];
                    })
                    .catch(err => {
                        console.error(error);
                        this.$message({
                            message: error,
                            type: 'error'
                        });
                    });
            },
            setCurrentFolder(folder) {
                console.log("setCurrentFolder: ",folder);
                if(folder.id){
                    this.$store.commit("storeCurrentNodeData", folder);
                }
            },
            selectionChanged(list){
                this.selectedNodes = list;
            },
            deleteNodes(){
                console.log("deleteNodes", this.selectedNodes);
                let self = this;
                if(this.selectedNodes.length===0)
                    return;;
                this.$confirm(`Voulez vous supprimer '${this.selectedNodes.map(e=>e.name).join(",")}' nodes ?`,'Warning', {
                    confirmButtonText: 'OK',
                    cancelButtonText: 'Annuler',
                    type: 'warning'
                })
                    .then(yes=>{
                        let nodesId = self.selectedNodes.map(elt=> elt.id);
                        self.$store.dispatch("deleteNodesById",{nodesId, recursive:true})
                            .then(ids=>{
                                console.log(ids);
                                if(ids.length>0){
                                    let deletedNodes = self.selectedNodes
                                        .filter(el=> _.findIndex(ids, (o)=> o === el.id)!==-1)
                                    self.$bus.$emit("nodes_deleted",deletedNodes);
                                    this.$message({
                                        message: `'${deletedNodes.map(e=>e.name).join(",")}' supprimés avec succes`,
                                        type: 'success'
                                    });
                                    self.selectedNodes=[];
                                }else{
                                    this.$message({
                                        message: `'Aucune noeud ni suprimée`,
                                        type: 'warning'
                                    });
                                }
                            })
                            .catch(error=>{
                                console.error(error);
                                this.$message({
                                    message: error,
                                    type: 'error'
                                });
                            });
                    });
            },
            moveNodes(operation){
                console.log("copyNodes : ",this.selectedNodes);
                if(this.selectedNodes.length>0)
                    this.$store.commit("setNodesInClipBoard",this.selectedNodes,operation||"COPY");
            },
            pasteNodes(){
                console.log("pasteNodes : ");
                this.$store.dispatch("pasteNodesIn",this.currentNodeData)
                    .then(response=>{
                        this.$message({
                            message: `'${response.name}' collé avec succes`,
                            type: 'success'
                        });
                        this.loadFolderContent();
                    })
                    .catch(error=>{
                        console.error(error);
                        this.$message({
                            message: error,
                            type: 'error'
                        });
                    });
            },
            openCreateFolderDialog(){
                this.$bus.$emit("create_folder",undefined);
            },
            download(){
                console.log("download: ", this.selectedNodes);
                // if one file is selected
                if(this.selectedNodes.length===1 && this.selectedNodes[0].file){
                    let node = this.selectedNodes[0];
                    this.$store.dispatch("streamNodeContent", node.id)
                        .then(response=>{
                            console.log(response);
                            this.startDownloading(response.data,response.contentType,this.selectedNodes[0].name);
                        })
                        .catch(error=>{
                            console.error(error);
                            this.$message({
                                message: error,
                                type: 'error'
                            });
                        })
                }
                // if many nodes are selected (files or folders)
                else{
                    this.$store.dispatch("getCompressedNodes",this.selectedNodes.map(elt=>elt.id))
                        .then(response=>{
                            //console.log(response);
                            setTimeout(()=>{
                                let blob = new Blob([response.data],{type:response.contentType});
                                let zipFile = new JSZip();
                                let reader = new FileReader();
                                reader.readAsDataURL(blob);
                                reader.onloadend = function() {
                                    let result = reader.result;
                                    let base64_marker = ";base64,",
                                        base64_index = result.indexOf(base64_marker),
                                        base64 = result.substring(base64_index + base64_marker.length); // result is base64
                                    zipFile.loadAsync(base64, {base64:true, checkCRC32: true});
                                    console.log(zipFile);
                                };
                                //this.startDownloading(response.data,response.contentType,"file.zip");
                            },2000)
                        })
                        .catch(error=>{
                            console.error(error);
                            this.$message({
                                message: error,
                                type: 'error'
                            });
                        });
                }
            },
            getFileContent(fileObject){
                return new Promise((resolve, reject) => {
                    let file = fileObject,
                        reader = new FileReader();

                    if(file && file.size > 0) {
                        reader.readAsDataURL(file);
                        reader.onloadend = (e) => {
                            resolve(e.target.result);
                        };
                        reader.onerror = (err) =>{
                            reject(err);
                        };
                    } else {
                        if (file.size === 0)
                            reject("empty file");
                        else
                            reject("error while getting file content");
                    }
                });
            },
            generateZipFile(zipObject, name){
                // generate zip file
                return new Promise((resolve, reject) => {
                    zipObject.generateAsync({
                        type:"blob",
                        compression: "DEFLATE",
                        compressionOptions: {
                            level: 9
                        }})
                        .then(function(blob) {
                            let file = new File([blob], name, {type:"application/zip"});
                            resolve(file);
                        })
                        .catch(err=>{
                            reject(err);
                        });
                });
            },
            startDownloading(content, type, name){
                //console.log(response);
                let blob = new Blob([content],{type});
                let file = new File([blob], name,{type});
                // this method
                let url = window.URL.createObjectURL(file);
                window.open(url, "_blank");
                console.log(file, blob, url);
                setTimeout(function () {
                    window.URL.revokeObjectURL(url);
                }, 1000);


                // or this method with FileReader
                /*
                let reader = new FileReader();
                reader.readAsDataURL(blob);
                reader.onloadend = function() {
                    let url = reader.result; // result is base64
                    let link = document.createElement("a");
                    link.setAttribute('download', name);
                    link.href = url;
                    document.body.appendChild(link);
                    link.click();
                    link.remove();
                    //window.open(url, "_blank");
                    console.log(file, blob, url);
                    setTimeout(function () {
                        window.URL.revokeObjectURL(url);
                    }, 1000);
                };
                reader.onerror = function(error){
                    console.error(error);
                    this.$message({
                        message: error,
                        type: 'error'
                    });
                }*/
            },

            goBackward(){
                if(this.foldersHistory.length>0){
                    let index = this.foldersHistory.findIndex(elt=> elt.id === this.currentNodeData.id);
                    if(index!==-1 && index>0)
                        this.setCurrentFolder(this.foldersHistory[index-1]);
                }
            },
            goForward(){
                if(this.foldersHistory.length>0){
                    let index = this.foldersHistory.findIndex(elt=> elt.id === this.currentNodeData.id);
                    if(index!==-1 && index<this.foldersHistory.length-1)
                        this.setCurrentFolder(this.foldersHistory[index+1]);
                }
            },

        }
    }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
    .el-breadcrumb {
        font-size: 14px;
        line-height: 2;
    }

    .el-breadcrumb >>> .el-breadcrumb__separator {
        margin: 0 3px;
        color: #66b1ff;
    }

    .el-breadcrumb >>> .el-breadcrumb__item {
        cursor: pointer;
    }
    .top-row{
        display: flex;
        justify-content: space-between;
    }

    .content{
        height: 90%;
        overflow-y: auto;
        margin-top: 3px;
    }

    .context-menu {
        position: fixed;
        background: white;
        z-index: 999;
        outline: none;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12), 0 1px 2px rgba(0, 0, 0, 0.24);
        cursor: pointer;
    }

    .folder_buttons_set{
        display: flex;
        justify-content: flex-end;
    }

    .footer-row{
        width: 100%;
        background-color: #f8f9fb;
        bottom: 5px;
        position: fixed;
        height: 30px;
        border-radius: 2px;
    }
</style>
