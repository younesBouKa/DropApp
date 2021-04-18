<template>
    <div style="height: 100%">
        <el-row class="top_buttons" v-show="showTopMenu">
            <el-button @click="openCreateFolderDialog" size="mini" type="primary" icon="el-icon-circle-plus" title="Add new space" circle></el-button>
            <el-button @click="deleteCurrentNode" size="mini" type="danger" icon="el-icon-delete" title="Delete Selected nodes" circle></el-button>
            <el-button @click="refreshTree" size="mini" type="success" icon="el-icon-refresh-right" title="Refresh" circle></el-button>
            <el-button disabled size="mini" type="info" icon="el-icon-more" title="Expand more" circle></el-button>
            <el-button disabled size="mini" type="warning" icon="el-icon-search" title="Deep search in current folder" circle></el-button>
        </el-row>
        <el-input
                class="search_input mini"
                clearable
                size="small"
                prefix-icon="el-icon-search"
                placeholder="Search .."
                v-model="filterText">
        </el-input>
        <el-tree
                class="tree"
                :data="data"
                :node-key="defaultProps.nodeKey"
                :default-expanded-keys="defaultExpandedKeys"
                :current-node-key="currentNodeData.id"
                :empty-text="'No Files'"
                :filter-node-method="filterNode"
                :props="defaultProps"
                @node-click="handleNodeClick"
                @check="onCheck"
                @current-change="handleCurrentChange"
                @node-drop="onNodeDrop"
                :highlight-current="true"
                accordion
                lazy
                :draggable="true"
                :allow-drag="allowDrag"
                :allow-drop="allowDrop"
                :load="loadNode"
                ref="tree"
                :show-checkbox="false"
        >
            <span class="custom-tree-node" slot-scope="{ node, data }">
                 <el-tooltip placement="right" effect="light">
                     <div slot="content">Here we put <br/>node description</div>
                     <span style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis !important;">
                            <i
                                    :class="data.folder ? 'el-icon-folder-opened' : 'el-icon-document'"
                            >
                            </i>
                            {{ node.label }}
                     </span>
                </el-tooltip>
          </span>
        </el-tree>
    </div>
</template>

<script>
    import {mapActions} from 'vuex'

    export default {
        name: "SpaceTree",
        props: {
            rootPath: {
                type: String,
                default : ()=> "/"
            },
            showTopMenu: {
                type: Boolean,
                default: ()=> true
            }
        },
        data() {
            return {
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
                defaultExpandedKeys: [],
                loading : undefined,
            }
        },
        computed: {
            currentNodeData() {
                return this.$store.getters.getCurrentNodeData;
            },
            sortByFolderFirst(){
                return this.$store.getters.getSortByFolderFirst;
            },
            currentNodeElement(){
                return this.$store.getters.getCurrentNodeElement;
            },
        },
        watch: {
            filterText(val) {
                this.$refs.tree.filter(val);
            },
            currentNodeData(val) {
                if (val.id && this.defaultExpandedKeys.indexOf(val.id) === -1)
                    this.defaultExpandedKeys = [val.id];
            },
            rootPath(val) {
                this.getRootFolder();
            },
            data(val){
                this.updateRootElement();
            }
        },
        mounted() {
            this.getRootFolder()
                .catch(error=>{
                    console.error(error);
                    this.$message({
                        message: error,
                        type: 'error'
                    });
                });
            let self = this;
            this.$bus.$on("files_uploaded", function (payLoad) {
                (payLoad||[]).forEach(node=>{
                    self.$refs.tree.append(node, node.parentId);
                })
            });

            this.$bus.$on("folder_created", function (payLoad) {
                self.$refs.tree.append(payLoad, payLoad.parentId);
            });

            this.$bus.$on("nodes_deleted", function (payLoad) {
                if(self.currentNodeElement && self.currentNodeElement.childNodes){
                    (payLoad||[]).forEach(node=>{
                        let index = self.currentNodeElement.childNodes.findIndex(elt=>elt.data.id===node.id);
                        if(index!==-1)
                            self.currentNodeElement.childNodes.splice(index,1);
                    })
                }
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
                this.$bus.$emit("create_folder",undefined);
            },
            refreshTree(){
                let selectedNode = this.currentNodeData;
                let self = this;
                this.getRootFolder()
                    .then(parent=>{
                        self.defaultExpandedKeys.push(selectedNode.id);
                    })
                    .catch(error=>{
                        console.error("getRootFolder",error);
                        this.$message({
                            message: error,
                            type: 'error'
                        });
                    });
            },

            deleteCurrentNode(){
                let self = this;
                this.$confirm(`Voulez vous supprimer '${this.currentNodeData.name}' ?`,'Warning', {
                    confirmButtonText: 'OK',
                    cancelButtonText: 'Annuler',
                    type: 'warning'
                })
                    .then(yes=>{
                        self.$store.dispatch("deleteNodeById",{nodeId: self.currentNodeData.id, recursive: true})
                            .then(count=>{
                                console.log("deleteCurrentNode",count);
                                this.$message({
                                    message: `'${this.currentNodeData.name}' supprimé avec succes`,
                                    type: 'success'
                                });
                                self.$bus.$emit("nodes_deleted",[self.currentNodeData]);
                                self.$store.commit("storeCurrentNodeData",undefined);
                                self.refreshTree();
                            })
                            .catch(error=>{
                                console.error("deleteCurrentNode",error);
                                this.$message({
                                    message: error,
                                    type: 'error'
                                });
                            })
                    });
            },
            getRootFolder() {
                this.data = [];
                let self = this;
                let rootPath = this.rootPath || "/";
                return new Promise((resolve, reject) => {
                    self.$store.dispatch("getNodeByPath",rootPath)
                        .then(data => {
                            console.log("getRootFolder",data);
                            if (data) {
                                self.rootFolder = data;
                                self.data.push(data);
                                self.$store.commit("storeCurrentNodeData", self.data[0]);
                                self.updateRootElement();
                            }
                            resolve(data);
                        }).catch(err => {
                            console.error("getRootFolder",err);
                            reject(err);
                        });
                });
            },

            filterNode(value, data, node) {
                console.log(`filterNode : `, value, data);
                if (!value) return true;
                return (data[this.defaultProps.label] + data[this.defaultProps.path])
                    .toLowerCase()
                    .indexOf(value) !== -1;
            },
            handleNodeClick(node) {
                console.log(`node clicked : `, node);
            },
            handleCurrentChange(data,node) {
                console.log(`current node changed : `, data, node);
                this.$store.commit("storeCurrentNodeElement", node);
                this.$store.dispatch("storeRootElement",this.$refs.tree.root);
            },

            updateRootElement(){
                this.$store.dispatch("storeRootElement",this.$refs.tree.root);
            },

            loadNode(node, resolve) {
                console.log(`loadNode: `, node);
                if(!node || !node.data || !node.data.id)
                    return;
                this.$store.dispatch("getNodesByParentId",node.data.id)
                    .then(nodes => {
                        console.log("loadNode",nodes);
                        nodes = nodes.sort(this.sortByFolderFirst);
                        resolve(nodes);
                    })
                    .catch(error => {
                        console.error("loadNode",error);
                        this.$message({
                            message: error,
                            type: 'error'
                        });
                        resolve([]);
                    });
            },
            onCheck(node, status) {
                /*
                Le noeud modifié,
                l'objet statut de l'arbre avec quatre propriétés: checkedNodes, checkedKeys, halfCheckedNodes, halfCheckedKeys.
                */
                //console.log(`oncheck : `, node, status);
            },

            onNodeDrop(movedNode, destNode, moveType) {
                // Le noeud déplacé, le noeud d'arrivée, le type de placement (before / after / inner), l'évènement.
                console.log(`onNodeDrop : `, movedNode, destNode, moveType);
                if(destNode.data.file && moveType==="inner")
                    return;
                let srcNode = movedNode.data,
                    targetNode;
                if(moveType==="inner")
                    targetNode = destNode.data;
                else
                    targetNode = destNode.parent.data;
                // confirm move operation
                let self = this;
                this.$confirm(`Do you want to move: '${srcNode.name}' to: '${targetNode.name}' ?`, "Warning", {
                    confirmButtonText: 'OK',
                    cancelButtonText: 'Annuler',
                    type: 'warning'
                })
                    .then(yes =>{
                        self.loading = this.$loading({
                            lock: true,
                            text: 'Loading',
                            spinner: 'el-icon-loading',
                            background: 'rgba(0, 0, 0, 0.7)'
                        });
                        return self.$store.dispatch("moveNodeById",{srcId: srcNode.id, destId: targetNode.id});
                    })
                    .then(response=>{
                        console.log("moveNodeById",response);
                        this.$message({
                            message: 'Node "'+srcNode.name+'" moved to "'+targetNode.name+'" width success.',
                            type: 'success'
                        });
                        self.$bus.$emit("node_moved",{srcNode, targetNode});
                    })
                    .catch(error=>{
                        console.error("moveNodeById",error);
                        this.$message({
                            message: error,
                            type: 'error'
                        });
                    })
                    .finally(()=>{
                        if(self.loading)
                            self.loading.close();
                    });
            },
            allowDrag(node) {
                //console.log(`allowDrag : `, node);
                return true;
            },
            allowDrop(draggingNode, dropNode, type) {
                //console.log(`allowDrop : `, draggingNode, dropNode, type);
                return !(dropNode.data.file && type === "inner");

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

    .tree {
        height: calc(100% - 60px);
        padding-bottom: 10px;
        overflow: auto;
    }

    .custom-tree-node{
        font-size: 10px;
    }

    .custom-tree-node >>> .el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content {
        background-color: #e3effd;
    }
</style>
