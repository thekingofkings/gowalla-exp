a = importdata('Gowalla_edges.txt');
b = importdata('frequent-pair.txt');

pair = b(:,1:2);
[c, ia, ib] = intersect(a, pair, 'rows');
friendLabel = zeros(size(b,1), 1);
friendLabel(ib) = 1;


features = importdata('feature-vectors.txt');
features = features';
nan_index = find(isnan(features));
features(nan_index) = 0;

fmean = repmat(min(features), size(features,1), 1);
fvariance = repmat(max(features-fmean), size(features,1), 1);
norm_features = (features - fmean) ./ fvariance;


mdl = LinearModel.fit(norm_features, friendLabel)