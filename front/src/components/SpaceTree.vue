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
                defaultExpandedKeys: []
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
                console.log(this.$refs.tree.root);
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
            this.getRootFolder();
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
                this.getRootFolder().then(parent=>{
                    self.defaultExpandedKeys.push(selectedNode.id);
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
                        self.deleteNodeById(self.currentNodeData.id,true)
                            .then(count=>{
                                console.log(count);
                                this.$message({
                                    message: `'${this.currentNodeData.name}' supprimé avec succes`,
                                    type: 'success'
                                });
                                self.$bus.$emit("nodes_deleted",[self.currentNodeData]);
                                self.$store.commit("storeCurrentNodeData",undefined);
                                self.refreshTree();
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
            getRootFolder() {
                this.data = [];
                let self = this;
                let rootPath = this.rootPath || "/";
                return new Promise((resolve, reject) => {
                    self.getNodeByPath(rootPath)
                        .then(data => {
                            console.log(data);
                            if (data) {
                                self.rootFolder = data;
                                self.data.push(data);
                                self.$store.commit("storeCurrentNodeData", self.data[0]);
                                self.updateRootElement();
                            }
                            resolve(data);
                        }).catch(err => {
                            console.error(err);
                            reject(err);
                        });
                });
            },
            append(data) {
                const newChild = {id: id++, label: 'testtest', children: []};
                if (!data.children) {
                    this.$set(data, 'children', []);
                }
                data.children.push(newChild);
            },
            remove(node, data) {
                const parent = node.parent;
                const children = parent.data.children || parent.data;
                const index = children.findIndex(d => d.id === data.id);
                children.splice(index, 1);
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
                this.getNodesByParentId(node.data.id)
                    .then(nodes => {
                        console.log(nodes);
                        nodes = nodes.sort(this.sortByFolderFirst);
                        resolve(nodes);
                    })
                    .catch(err => {
                        resolve([]);
                    });
            },
            onCheck(node, status) {
                /*
                Le noeud modifié,
                l'objet statut de l'arbre avec quatre propriétés: checkedNodes, checkedKeys, halfCheckedNodes, halfCheckedKeys.
                */
                console.log(`oncheck : `, node, status);
            },
            onNodeDrop(movedNode, destNode, moveType) {
                /*
                Le noeud déplacé, le noeud d'arrivée, le type de placement (before / after / inner), l'évènement.
                 */
                console.log(`onNodeDrop : `, movedNode, destNode, moveType)
            },
            allowDrag(node) {
                console.log(`allowDrag : `, node);
                return true;
            },
            allowDrop(draggingNode, dropNode, type) {
                console.log(`allowDrop : `, draggingNode, dropNode, type);
                return true;
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
