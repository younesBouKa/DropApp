<template>
    <div>
        <el-input
                clearable
                prefix-icon="el-icon-search"
                small
                placeholder="Filter keyword"
                v-model="filterText">
        </el-input>
        <el-tree
                :data="data"
                :empty-text="'No Spaces'"
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
                <span>{{ node.label }}</span>
                <span>
                  *
                </span>
          </span>
        </el-tree>
    </div>
</template>

<script>
    export default {
        name: "SpaceTree",
        props: {
            msg: String
        },
        data() {
            return {
                filterText: "",
                data: [
                    {
                        label: 'Niveau un 1',
                        children: [{
                            label: 'Niveau deux 1-1',
                            children: [{
                                label: 'Niveau trois 1-1-1'
                            }]
                        }]
                    },
                    {
                        label: 'Niveau un 2',
                        children: [{
                            label: 'Niveau deux 2-1',
                            children: [{
                                label: 'Niveau trois 2-1-1'
                            }]
                        }, {
                            label: 'Niveau deux 2-2',
                            children: [{
                                label: 'Niveau trois 2-2-1'
                            }]
                        }]
                    },
                    {
                        label: 'Niveau un 3',
                        children: [{
                            label: 'Niveau deux 3-1',
                            children: [{
                                label: 'Niveau trois 3-1-1'
                            }]
                        }, {
                            label: 'Niveau deux 3-2',
                            children: [{
                                label: 'Niveau trois 3-2-1'
                            }]
                        }]
                    }
                ],
                defaultProps: {
                    children: 'children',
                    label: 'label'
                }
            }
        },
        computed: {},
        watch: {
            filterText(val) {
                this.$refs.tree.filter(val);
            }
        },
        methods: {
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
                return data.label.indexOf(value) !== -1;
            },
            handleNodeClick(node) {
                console.log(`node clicked : `,node);
            },
            loadNode(node, resolve) {
                console.log(`loadNode: `,node);
                if (node.level === 0) {
                    return resolve([{ label: 'region' }]);
                }
                if (node.level > 1) return resolve([]);

                setTimeout(() => {
                    const data = [{
                        label: 'leaf',
                        leaf: true
                    }, {
                        label: 'zone'
                    }];

                    resolve(data);
                }, 500);
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

</style>
