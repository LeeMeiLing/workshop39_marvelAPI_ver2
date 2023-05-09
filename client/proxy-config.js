// ==== For Angular run locally , server on railway ====
module.exports = [
    {
        context: ['/**'],
        target: 'https://astute-education-production.up.railway.app',
        secure: true,
        changeOrigin: true
    }
]

// ==== For running locally ====
// module.exports = [
//     {
//         context: ['/**'],
//         target: 'http://localhost:8080',
//         secure: false
//     }
// ]