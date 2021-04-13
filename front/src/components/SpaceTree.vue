<template>
    <div style="height: 100%">
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
                show-checkbox
        >
            <span class="custom-tree-node" slot-scope="{ node, data }">
                 <el-tooltip  placement="right" effect="light">
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
    import { mapActions } from 'vuex'
    export default {
        name: "SpaceTree",
        props: {
            msg: String
        },
        data() {
            return {
                filterText: "",
                data: [],
                defaultProps: {
                    children: 'children',
                    label: 'name'
                }
            }
        },
        computed: {},
        watch: {
            filterText(val) {
                this.$refs.tree.filter(val);
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
            getRootFolder(){
                this.data = [];
                this.getNodeByPath("/")
                    .then(data=>{
                        console.log(data);
                        this.data.push(data);
                    }).catch(err=>{
                    console.error(err);
                });
            },
            append(data) {
                const newChild = { id: id++, label: 'testtest', children: [] };
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
                console.log(`filterNode : `,value,data);
                if (!value) return true;
                return data[this.defaultProps.label].toLowerCase().indexOf(value) !== -1;
            },
            handleNodeClick(node) {
                console.log(`node clicked : `,node);
                this.$store.commit("storeCurrentNode",node);
            },
            loadNode(node, resolve) {
                console.log(`loadNode: `,node);
                this.getNodesByParentId(node.data.id)
                    .then(nodes=>{
                        console.log(nodes);
                        resolve(nodes);
                    })
                    .catch(err=>{
                        resolve([]);
                    });
            },
            onCheck(node, status) {
                /*
                Le noeud modifié,
                l'objet statut de l'arbre avec quatre propriétés: checkedNodes, checkedKeys, halfCheckedNodes, halfCheckedKeys.
                */
                console.log(`oncheck : `,node,status);
            },
            onNodeDrop(movedNode, destNode, moveType){
                /*
                Le noeud déplacé, le noeud d'arrivée, le type de placement (before / after / inner), l'évènement.
                 */
                console.log(`onNodeDrop : `,movedNode,destNode,moveType)
            },
            allowDrag(node){
                console.log(`allowDrag : `,node);
                return true;
            },
            allowDrop(draggingNode, dropNode, type){
                console.log(`allowDrop : `,draggingNode,dropNode,type);
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
    .search_input{
        height: 35px;
        margin-bottom: 5px;
    }

    .tree{
        height: calc(100% - 40px);
        padding-bottom: 10px;
        overflow: auto;
    }
</style>
