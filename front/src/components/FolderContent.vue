<template>
    <div style="height: 100%">
        <el-row class="top-row">
            <el-col>
                <el-breadcrumb separator="/">
                    <el-breadcrumb-item
                            v-for="folder in pathFolders"
                            @click="setCurrentNode(folder)"
                    >
                        {{folder}}
                    </el-breadcrumb-item>
                </el-breadcrumb>
            </el-col>
            <el-col class="folder_buttons_set">
                <div>
                    <el-button @click="isListView=!isListView" size="mini"  :icon="isListView?'el-icon-files':'el-icon-s-grid'" title="Switch views" circle></el-button>
                    <el-button @click="openCreateFolderDialog" size="mini" type="primary" icon="el-icon-circle-plus" title="Create folder" circle></el-button>
                    <el-button @click="enableUpload=!enableUpload" size="mini" :type="!enableUpload? 'success' : 'warning'" icon="el-icon-upload" title="Upload file" circle></el-button>
                    <el-button  disabled size="mini" type="danger" icon="el-icon-delete" title="Delete Selected nodes" circle></el-button>
                    <el-button @click="loadFolderContent" size="mini" type="info" icon="el-icon-refresh-right" title="Refresh" circle></el-button>
                    <el-button size="mini" type="warning" icon="el-icon-search" title="Search" circle></el-button>
                </div>
            </el-col>
        </el-row>
        <el-row class="content" >
            <UploadFiles v-if="canUploadFileInFolder"></UploadFiles>
            <TableView v-else-if="isListView && isSelectedNodeFolder" :rows="data" :columns="columnsToShow"></TableView>
            <CardsView v-else-if="isSelectedNodeFolder" :data="dataRows"></CardsView>
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
            folderId: String,
        },
        data() {
            return {
                isListView : false,
                enableUpload : false,
                data: [],
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
            selectedNode(){
                return this.$store.getters.getCurrentNode;
            },
            pathFolders(){
                return this.selectedNode &&  this.selectedNode.path ? this.selectedNode.path.split("/") : [] ;
            },
            isSelectedNodeFolder(){
               return this.selectedNode.folder;
            },
            isFolderEmpty(){
               return this.isSelectedNodeFolder && this.data.length===0;
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
           /* ...mapGetters({
                selectedNode:"getCurrentNode"
            }),*/
        },
        watch: {
            selectedNode(val) {
                this.loadFolderContent();
            }
        },
        mounted() {
            this.loadFolderContent();
            let self = this;
            this.$bus.$on("file_uploaded", function (payLoad) {
                self.loadFolderContent();
            });
            this.$bus.$on("folder_created", function (payLoad) {
                /*if(payLoad.parentId===this.selectedNode.id){
                    this.data.push(payLoad);
                }*/
                self.loadFolderContent();
            });
            this.$bus.$on("node_deleted", function (payLoad) {
                let index = this.data.findIndex(elt=> elt.id===payLoad.id);
                if(index!==-1)
                    this.data.splice(index,1);
            });
        },
        methods: {
            ...mapActions({
                getNodesByParentId: 'getNodesByParentId',
            }),
            loadFolderContent() {
                console.log("loadFolderContent : "+this.selectedNode);
                this.data = [];
                if (!this.selectedNode || !this.selectedNode.id || !this.selectedNode.folder)
                    return;
                this.getNodesByParentId(this.selectedNode.id)
                    .then(nodes => {
                        console.log(nodes);
                        this.data = nodes;
                    })
                    .catch(err => {
                        this.data = [];
                    });
            },
            setCurrentNode(row) {
                this.$store.commit("storeCurrentNode", row);
            },
            openCreateFolderDialog(){
                this.$bus.$emit("create_folder",undefined);
            },
        }
    }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
    .el-breadcrumb >>> .el-breadcrumb__separator {
        margin: 0 3px;
        color: #66b1ff;
    }

    .el-breadcrumb >>> .el-breadcrumb__item {
        cursor: pointer;
    }

    .content{
        height: 93%;
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
</style>
