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
                    <el-button @click="enableUpload=!enableUpload" size="mini" :type="!enableUpload? 'success' : 'warning'" icon="el-icon-upload" title="Upload file" circle></el-button>
                    <el-button @click="openCreateFolderDialog" type="primary" size="mini" icon="el-icon-folder-add" title="Create folder" circle></el-button>
                    <el-divider direction="vertical"></el-divider>
                    <el-button @click="moveNodes" disabled size="mini" type="warning" icon="el-icon-back" title="Deplacer" circle></el-button>
                    <el-button @click="copyNodes" disabled size="mini" type="info" icon="el-icon-document-copy" title="Copier" circle></el-button>
                    <el-button @click="deleteNodes" :disabled="selectedNodes.length===0" size="mini" type="danger" icon="el-icon-delete" title="Delete Selected nodes" circle></el-button>
                    <el-divider direction="vertical"></el-divider>
                    <el-button @click="isListView=!isListView" size="mini"  :icon="isListView?'el-icon-files':'el-icon-s-grid'" title="Switch views" circle></el-button>
                    <el-button @click="loadFolderContent" size="mini" type="info" icon="el-icon-refresh-right" title="Refresh" circle></el-button>
                    <el-button size="mini" type="warning" icon="el-icon-search" title="Search" circle></el-button>
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
                isListView : false,
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
           /* ...mapGetters({
                currentNodeData:"getCurrentNodeData"
            }),*/
        },
        watch: {
            currentNodeData(val) {
                this.loadFolderContent();
                this.selectedNodes=[];
            },
            folderId(val) {
                this.loadFolderContent();
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
                this.getNodesByParentId(folderId)
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
                this.$confirm(`Voulez vous supprimer '${this.selectedNodes.length}' nodes ?`,'Warning', {
                    confirmButtonText: 'OK',
                    cancelButtonText: 'Annuler',
                    type: 'warning'
                })
                    .then(yes=>{
                        let nodesId = self.selectedNodes.map(elt=> elt.id);
                        self.deleteNodesById(nodesId, true)
                            .then(ids=>{
                                console.log(ids);
                                this.$message({
                                    message: `'${ids.length}' noeuds supprimés avec succes`,
                                    type: 'success'
                                });
                                ;
                                let deletedNodes = self.selectedNodes
                                    .filter(el=> _.findIndex(ids, (o)=> o === el.id)!==-1)
                                self.$bus.$emit("nodes_deleted",deletedNodes);
                                self.selectedNodes=[];
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
            copyNodes(){

            },
            moveNodes(){

            },
            openCreateFolderDialog(){
                this.$bus.$emit("create_folder",undefined);
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

    .content{
        height: 90%;
        overflow-y: auto;
        margin-top: 3px;
    }

    .top-row{
        display: flex;
        justify-content: space-between;
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
