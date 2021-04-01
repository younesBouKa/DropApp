<template>
    <div id="app" @dragover.prevent @drop.prevent>
        <div class="container" @drop="handleFileDrop">
            Add your files here:
            <br>
            <div class="file-wrapper">
                <input type="file" name="file-input"
                       multiple="True" @change="handleFileInput">
                Click or drag to insert.
                <progress max="100" :value.prop="uploadPercentage"></progress>
            </div>
            <ul>
                <li v-for="(file, index) in files">
                    {{ file.name }} ({{ file.size }} b)
                    <button @click="removeFile(index)"
                            title="Remove">X
                    </button>
                </li>
            </ul>
        </div>
    </div>
</template>

<script>
    export default {
        name: 'HelloWorld',
        props: {
            msg: String
        },
        data() {
            return {
                files: []
            }
        },
        computed: {
            uploadDisabled() {
                return this.files.length === 0;
            }
        },
        methods: {
            addFile(e) {
                let droppedFiles = e.dataTransfer.files;
                if (!droppedFiles) return;
                // this tip, convert FileList to array, credit: https://www.smashingmagazine.com/2018/01/drag-drop-file-uploader-vanilla-js/
                ([...droppedFiles]).forEach(f => {
                    this.files.push(f);
                });
            },
            handleFileDrop(e) {
                let droppedFiles = e.dataTransfer.files;
                if (!droppedFiles) return;
                ([...droppedFiles]).forEach(f => {
                    this.files.push(f);
                });
            },
            handleFileInput(e) {
                let files = e.target.files
                if (!files) return;
                ([...files]).forEach(f => {
                    this.files.push(f);
                });
            },
            removeFile(fileKey) {
                this.files.splice(fileKey, 1)
            },
            upload() {

                let formData = new FormData();
                this.files.forEach((f, x) => {
                    formData.append('file' + (x + 1), f);
                });

                axios.post('https://httpbin.org/post',
                    formData,
                    {
                        headers: {
                            'Content-Type': 'multipart/form-data'
                        },
                        onUploadProgress: function (progressEvent) {
                            this.uploadPercentage = parseInt(Math.round((progressEvent.loaded * 100) / progressEvent.total));
                        }.bind(this)
                    }
                ).then(function () {
                    console.log('SUCCESS!!');
                })
                    .catch(function () {
                        console.log('FAILURE!!');
                    });

            }
        }
    }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
    h3 {
        margin: 40px 0 0;
    }

    ul {
        list-style-type: none;
        padding: 0;
    }

    li {
        display: inline-block;
        margin: 0 10px;
    }

    a {
        color: #42b983;
    }

    .file-wrapper {
        text-align: center;
        width: 200px;
        height: 3em;
        vertical-align: middle;
        display: table-cell;
        position: relative;
        overflow: hidden;
        background: gray; /* and other things to make it pretty */
    }

    .file-wrapper input {
        position: absolute;
        top: 0;
        right: 0;
        cursor: pointer;
        opacity: 0.0;
        filter: alpha(opacity=0);
        font-size: 300px;
        height: 200px;
    }

    progress {
        width: 400px;
        margin: auto;
        display: block;
        margin-top: 20px;
        margin-bottom: 20px;
    }
</style>
