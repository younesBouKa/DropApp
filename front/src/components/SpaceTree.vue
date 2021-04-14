<template>
    <div style="height: 100%">
        <el-row class="top_buttons">
            <el-button @click="dialogVisible = true" size="mini" type="primary" icon="el-icon-circle-plus" title="Add new space" circle></el-button>
            <el-button size="mini" type="info" icon="el-icon-more" title="Expand more" circle></el-button>
            <el-button size="mini" type="success" icon="el-icon-refresh-right" title="Refresh" circle></el-button>
            <el-button size="mini" type="warning" icon="el-icon-search" title="Deep search in current folder"
                       circle></el-button>
            <el-button size="mini" type="danger" icon="el-icon-delete" title="Delete Selected nodes" circle></el-button>
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
                :current-node-key="currentNode.id"
                :empty-text="'No Files'"
                :filter-node-method="filterNode"
                :props="defaultProps"
                @node-click="handleNodeClick"
                @check="onCheck"
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

        <el-dialog
                title="Create new Folder"
                :visible.sync="dialogVisible"
                width="30%"
                :before-close="handleCloseFolderCreationDialog">
            <span>Ceci est un message</span>
            <span slot="footer" class="dialog-footer">
                <el-button @click="dialogVisible = false">Annuler</el-button>
                <el-button type="primary" @click="dialogVisible = false">Confirmer</el-button>
            </span>
        </el-dialog>
    </div>
</template>

<script>
    import {mapActions} from 'vuex'

    export default {
        name: "SpaceTree",
        props: {
            msg: String
        },
        data() {
            return {
                dialogVisible: false,
                filterText: "",
                data: [],
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
            this.getRootFolder();
            let self = this;
            this.$bus.$on("file_uploaded", function (payLoad) {
                self.getRootFolder();
            });
        },
        methods: {
            ...mapActions([
                'getNodeById',
                'getNodeByPath',
                'getNodesByParentPath',
                'getNodesByParentId',
            ]),
            handleCloseFolderCreationDialog(done){
                console.log("handleCloseFolderCreationDialog", done);
                done();
            },
            getRootFolder() {
                this.data = [];
                this.getNodeByPath("/")
                    .then(data => {
                        console.log(data);
                        if (data) {
                            this.data.push(data);
                            this.$store.commit("storeCurrentNode", this.data[0]);
                        }
                    }).catch(err => {
                    console.error(err);
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
                this.$store.commit("storeCurrentNode", node);
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
    .search_input {
        height: 30px;
        margin-bottom: 5px;
    }

    .search_input >>> input, .search_input >>> i {
        height: 24px !important;
        line-height: 24px !important;
    }

    .tree {
        height: calc(100% - 35px);
        padding-bottom: 10px;
        overflow: auto;
    }

    .custom-tree-node{
        font-size: 10px;
    }

    .top_buttons {
        display: flex;
        height: 30px !important;
        justify-content: flex-end;
        padding: 5px 10px;
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
</style>
